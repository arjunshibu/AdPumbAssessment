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
            String line;
            while ((line = reader.readLine()) != null) {
                StringBuilder reqBuilder = new StringBuilder();
                reqBuilder.append(line).append("\r\n");
                while (!(line = reader.readLine()).isEmpty()) {
                    reqBuilder.append(line).append("\r\n");
                }
                reqBuilder.append("\r\n");
                String request = reqBuilder.toString();

                String firstLine = request.split("\r\n")[0];
                logger.info("Received request: " + firstLine);

                synchronized (offshoreOut) {
                    offshoreOut.writeInt(request.length());
                    offshoreOut.write(request.getBytes());
                    offshoreOut.flush();
                }

                synchronized (offshoreIn) {
                    int len = offshoreIn.readInt();
                    byte[] response = new byte[len];
                    offshoreIn.readFully(response);
                    clientOut.write(response);
                    clientOut.flush();
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
            client.close();
        } catch (IOException e) {
            logger.severe("Error closing client socket: " + e.getMessage());
        }
    }
}