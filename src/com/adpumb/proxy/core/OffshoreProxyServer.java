package com.adpumb.proxy.core;

import com.adpumb.proxy.command.Command;
import com.adpumb.proxy.command.CommandFactory;
import com.adpumb.proxy.config.Config;
import com.adpumb.proxy.config.LoggerManager;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class OffshoreProxyServer extends AbstractProxyComponent {
    private final Config config = Config.getInstance();
    private static final Logger logger = LoggerManager.getInstance(OffshoreProxyServer.class.getName());

    private OffshoreProxyServer() {
    }

    public static void main(String[] args) {
        OffshoreProxyServer server = new OffshoreProxyServer();
        server.start();
    }

    @Override
    public void start() {
        try (ServerSocket serverSocket = this.createServerSocket(config.getServerPort(), "Offshore Proxy Server")) {
            serverSocket.setReuseAddress(true);
            this.startConnectionAcceptor(serverSocket);
            this.processCommands();
        } catch (IOException e) {
            logger.severe("Error starting Offshore Proxy Server: " + e.getMessage());
        } finally {
            this.shutdown();
        }
    }

    @Override
    public void startConnectionAcceptor(ServerSocket serverSocket, Object... args) {
        this.getExecutorService().submit(() -> this.acceptConnections(serverSocket));
    }

    @Override
    public void handleNewConnection(ServerSocket serverSocket, Object... args)
            throws IOException, InterruptedException {
        Socket clientSocket = serverSocket.accept();
        logger.info("Client connected from: " + clientSocket.getRemoteSocketAddress());
        Command command = CommandFactory.getInstance().createServerCommand(clientSocket);
        this.getEventBus().publish(command);
    }

    private void acceptConnections(ServerSocket serverSocket, Object... args) {
        while (!serverSocket.isClosed()) {
            try {
                this.handleNewConnection(serverSocket);
            } catch (IOException e) {
                this.handleAcceptError(serverSocket, e);
            } catch (InterruptedException e) {
                this.handleInterruption("Connection acceptance interrupted, shutting down");
                break;
            }
        }
        logger.info("Offshore server socket closed, stopping acceptance of new connections");
    }
}
