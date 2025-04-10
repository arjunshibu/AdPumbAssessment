import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientCommand extends AbstractCommand {
    private final Socket clientSocket;
    private final DataInputStream proxyInputStream;
    private final DataOutputStream proxyOutputStream;
    private final RequestHandler requestHandler;
    private static final Logger logger = LoggerManager.getInstance(ClientCommand.class.getName());

    public ClientCommand(Socket clientSocket, DataInputStream proxyInputStream, DataOutputStream proxyOutputStream,
            RequestHandler requestHandler) {
        this.clientSocket = clientSocket;
        this.proxyInputStream = proxyInputStream;
        this.proxyOutputStream = proxyOutputStream;
        this.requestHandler = requestHandler;
    }

    @Override
    protected void processRequests() throws IOException {
        RequestContext context = new RequestContext.Builder()
                .withClientSocket(clientSocket)
                .withProxyInputStream(proxyInputStream)
                .withProxyOutputStream(proxyOutputStream)
                .build();

        requestHandler.handleRequest(context);
    }

    @Override
    protected void handleError(Exception e) {
        logger.severe("Error handling client: " + e.getMessage());
    }

    @Override
    protected void cleanup() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.severe("Error closing client socket: " + e.getMessage());
        }
    }
}