import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Logger;

public class ServerCommand extends AbstractCommand {
    private final Socket shipSocket;
    private static final Logger logger = LoggerManager.getInstance(ServerCommand.class.getName());

    public ServerCommand(Socket shipSocket) {
        this.shipSocket = shipSocket;
    }

    @Override
    protected void processRequests() throws IOException {
        try (DataInputStream in = new DataInputStream(shipSocket.getInputStream());
                DataOutputStream out = new DataOutputStream(shipSocket.getOutputStream())) {

            while (true) {
                int length;
                try {
                    length = in.readInt();
                } catch (EOFException eof) {
                    logger.info("Client disconnected");
                    break;
                }

                byte[] requestBytes = new byte[length];
                in.readFully(requestBytes);
                String request = new String(requestBytes);
                String firstLine = request.split("\r\n")[0];
                logger.info("Received request: " + firstLine);

                if (firstLine.toLowerCase().startsWith("connect")) {
                    rejectHttps(out);
                } else {
                    handleHttpRequest(request, out);
                }
            }
        }
    }

    @Override
    protected void handleError(Exception e) {
        logger.severe("Error handling client: " + e.getMessage());
    }

    @Override
    protected void cleanup() {
        try {
            shipSocket.close();
        } catch (IOException e) {
            logger.severe("Error closing server socket: " + e.getMessage());
        }
    }

    private void rejectHttps(DataOutputStream out) {
        try {
            String response = "HTTP/1.1 501 Not Implemented\r\nConnection: close\r\n\r\n";
            out.writeInt(response.length());
            out.write(response.getBytes());
            out.flush();
        } catch (IOException e) {
            logger.severe("Error sending HTTPS rejection: " + e.getMessage());
        }
    }

    private void handleHttpRequest(String request, DataOutputStream out) {
        try {
            String[] requestLines = request.split("\r\n");
            String[] requestLineParts = requestLines[0].split(" ");
            String urlStr = requestLineParts[1];
            URL url = new URL(urlStr);
            int port = url.getPort() == -1 ? 80 : url.getPort();

            try (Socket target = new Socket(url.getHost(), port)) {
                OutputStream targetOut = target.getOutputStream();
                InputStream targetIn = target.getInputStream();

                targetOut.write(request.getBytes());
                targetOut.flush();

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[8192];
                int n;
                while ((n = targetIn.read(data)) != -1) {
                    buffer.write(data, 0, n);
                    if (targetIn.available() == 0)
                        break;
                }

                byte[] responseBytes = buffer.toByteArray();
                synchronized (OffshoreProxyServer.outLock) {
                    out.writeInt(responseBytes.length);
                    out.write(responseBytes);
                    out.flush();
                }
            }

        } catch (Exception e) {
            logger.severe("Error handling HTTP request: " + e.getMessage());
        }
    }
}