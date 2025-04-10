import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class CommandFactory {
    private static CommandFactory instance;
    private final RequestHandler clientHandler;
    private final RequestHandlingStrategy requestStrategy;
    private final RequestHandler httpHandler;
    private final RequestHandler httpsHandler;

    private CommandFactory() {
        httpHandler = HttpRequestHandler.getInstance();
        httpsHandler = HttpsRequestHandler.getInstance();
        clientHandler = HttpClientHandler.getInstance();

        ProxyRequestStrategy strategy = ProxyRequestStrategy.getInstance();
        strategy.setHandlers(httpHandler, httpsHandler);
        requestStrategy = strategy;
    }

    public static CommandFactory getInstance() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }

    // Factory method for creating ServerCommand
    public Command createServerCommand(Socket socket) {
        return new ServerCommand(socket, requestStrategy);
    }

    // Factory method for creating ClientCommand
    public Command createClientCommand(Socket clientSocket, DataInputStream proxyServerInputStream,
            DataOutputStream proxyServerOutputStream) {
        return new ClientCommand(clientSocket, proxyServerInputStream, proxyServerOutputStream, clientHandler);
    }
}