package com.adpumb.proxy.core;

import com.adpumb.proxy.command.Command;
import com.adpumb.proxy.command.CommandFactory;
import com.adpumb.proxy.config.Config;
import com.adpumb.proxy.config.LoggerManager;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class ShipProxyClient extends AbstractProxyComponent {
    private Socket proxySocket;
    private DataInputStream proxyInputStream;
    private DataOutputStream proxyOutputStream;
    private final Config config = Config.getInstance();
    private static final Logger logger = LoggerManager.getInstance(ShipProxyClient.class.getName());

    private ShipProxyClient() {
    }

    public static void main(String[] args) {
        ShipProxyClient client = new ShipProxyClient();
        client.start();
    }

    @Override
    public void start() {
        try {
            // Establish the single TCP connection that will be reused
            this.proxySocket = connectToOffshoreServer();
            this.proxyInputStream = new DataInputStream(proxySocket.getInputStream());
            this.proxyOutputStream = new DataOutputStream(proxySocket.getOutputStream());

            try (ServerSocket localProxy = createServerSocket(config.getLocalPort(), "Ship Proxy Client")) {
                this.startConnectionAcceptor(localProxy, proxyInputStream, proxyOutputStream);
                this.processCommands();
            }
        } catch (IOException e) {
            logger.severe("Error connecting to Offshore Proxy Server: " + e.getMessage());
        } finally {
            closeProxyConnection();
            this.shutdown();
        }
    }

    @Override
    public void startConnectionAcceptor(ServerSocket serverSocket, Object... args) {
        this.getExecutorService()
                .submit(() -> this.acceptConnections(serverSocket, proxyInputStream, proxyOutputStream));
    }

    private void acceptConnections(ServerSocket serverSocket, Object... args) {
        DataInputStream proxyInputStream = (DataInputStream) args[0];
        DataOutputStream proxyOutputStream = (DataOutputStream) args[1];

        while (!serverSocket.isClosed()) {
            try {
                this.handleNewConnection(serverSocket, proxyInputStream, proxyOutputStream);
            } catch (IOException e) {
                this.handleAcceptError(serverSocket, e);
            } catch (InterruptedException e) {
                this.handleInterruption("Connection acceptance interrupted, shutting down");
                break;
            }
        }
        logger.info("Client socket closed, stopping acceptance of new connections");
    }

    @Override
    public void handleNewConnection(ServerSocket serverSocket, Object... args)
            throws IOException, InterruptedException {
        Socket clientSocket = serverSocket.accept();
        Command command = CommandFactory.getInstance()
                .createClientCommand(clientSocket, proxyInputStream, proxyOutputStream);
        this.getEventBus().publish(command);
    }

    private Socket connectToOffshoreServer() throws IOException {
        Socket proxySocket = new Socket(config.getServerHost(), config.getServerPort());
        logger.info("Connected to Offshore Proxy Server");
        return proxySocket;
    }

    private void closeProxyConnection() {
        try {
            if (proxyInputStream != null)
                proxyInputStream.close();
            if (proxyOutputStream != null)
                proxyOutputStream.close();
            if (proxySocket != null)
                proxySocket.close();
        } catch (IOException e) {
            logger.warning("Error closing proxy connection: " + e.getMessage());
        }
    }
}
