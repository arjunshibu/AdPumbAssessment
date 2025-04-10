import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class OffshoreProxyServer {
    private static final Logger logger = LoggerManager.getInstance(OffshoreProxyServer.class.getName());
    static final Object outLock = new Object();
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final EventBus eventBus = EventBus.getInstance();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9090);
            logger.info("Offshore Proxy Server running on port 9090");

            final ServerSocket finalServerSocket = serverSocket;
            executorService.submit(() -> {
                while (!finalServerSocket.isClosed()) {
                    try {
                        Socket shipSocket = finalServerSocket.accept();
                        logger.info("Client connected from: " + shipSocket.getRemoteSocketAddress());
                        Command command = CommandFactory.getInstance().createServerCommand(shipSocket);
                        eventBus.publish(command);
                    } catch (IOException e) {
                        if (finalServerSocket.isClosed()) {
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
            logger.severe("Error in server socket: " + e.getMessage());
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.severe("Error closing server socket: " + e.getMessage());
                }
            }
            executorService.shutdown();
        }
    }
}