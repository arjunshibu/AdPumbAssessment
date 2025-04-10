import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.logging.*;

public class ShipProxyClient {
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final EventBus eventBus = EventBus.getInstance();
    private static final Logger logger = LoggerManager.getInstance(ShipProxyClient.class.getName());

    public static void main(String[] args) {
        ServerSocket localProxy = null;
        try (Socket offshoreSocket = new Socket("0.0.0.0", 9090);
                DataInputStream offshoreIn = new DataInputStream(offshoreSocket.getInputStream());
                DataOutputStream offshoreOut = new DataOutputStream(offshoreSocket.getOutputStream())) {

            localProxy = new ServerSocket(8080);
            logger.info("Connected to Offshore Proxy Server");
            logger.info("Ship Proxy Client running on port 8080");

            final ServerSocket finalLocalProxy = localProxy;
            executorService.submit(() -> {
                while (!finalLocalProxy.isClosed()) {
                    try {
                        Socket clientSocket = finalLocalProxy.accept();
                        Command command = CommandFactory.createClientCommand(clientSocket, offshoreIn, offshoreOut);
                        eventBus.publish(command);
                    } catch (IOException e) {
                        if (finalLocalProxy.isClosed()) {
                            logger.info("Server socket closed, stopping acceptance of new connections.");
                            break;
                        }
                        logger.severe("Error accepting connection: " + e.getMessage());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.info("Connection acceptance interrupted, shutting down");
                        break;
                    }
                }
                logger.info("Client socket closed, stopping acceptance of new connections.");
            });

            while (true) {
                try {
                    Command command = eventBus.take();
                    command.execute();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Command processing interrupted, shutting down");
                    break;
                }
            }

        } catch (IOException e) {
            logger.severe("Error connecting to Offshore Proxy Server: " + e.getMessage());
        } finally {
            if (localProxy != null && !localProxy.isClosed()) {
                try {
                    localProxy.close();
                } catch (IOException e) {
                    logger.severe("Error closing server socket: " + e.getMessage());
                }
            }
            executorService.shutdown();
        }
    }
}
