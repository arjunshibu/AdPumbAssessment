import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientCommand extends AbstractCommand {
    private final Socket client;
    private final DataInputStream offshoreIn;
    private final DataOutputStream offshoreOut;
    private static final Logger logger = LoggerManager.getInstance(ClientCommand.class.getName());

    public ClientCommand(Socket client, DataInputStream offshoreIn, DataOutputStream offshoreOut) {
        this.client = client;
        this.offshoreIn = offshoreIn;
        this.offshoreOut = offshoreOut;
    }

    @Override
    protected void processRequests() throws IOException {
        try (
                InputStream clientIn = client.getInputStream();
                OutputStream clientOut = client.getOutputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn));

            // Read the first line to check if it's a valid request
            String firstLine = reader.readLine();
            if (firstLine == null) {
                return; // Connection closed
            }

            // Build the request
            StringBuilder reqBuilder = new StringBuilder();
            reqBuilder.append(firstLine).append("\r\n");

            // Read headers and find content length
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                reqBuilder.append(line).append("\r\n");
            }
            reqBuilder.append("\r\n");

            String request = reqBuilder.toString();
            logger.info("Received request: " + firstLine);

            // Process the request
            synchronized (offshoreOut) {
                offshoreOut.writeInt(request.length());
                offshoreOut.write(request.getBytes());
                offshoreOut.flush();
            }

            // Read response
            synchronized (offshoreIn) {
                int len = offshoreIn.readInt();
                if (len <= 0) {
                    throw new IOException("Invalid response length: " + len);
                }

                byte[] response = new byte[len];
                int totalRead = 0;
                while (totalRead < len) {
                    int read = offshoreIn.read(response, totalRead, len - totalRead);
                    if (read == -1) {
                        throw new IOException("Unexpected end of stream while reading response");
                    }
                    totalRead += read;
                }

                clientOut.write(response);
                clientOut.flush();
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
            if (client != null && !client.isClosed()) {
                client.close();
            }
        } catch (IOException e) {
            logger.severe("Error closing client socket: " + e.getMessage());
        }
    }
}