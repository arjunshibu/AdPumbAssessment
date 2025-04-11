package com.adpumb.proxy.core;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import com.adpumb.proxy.config.LoggerManager;
import com.adpumb.proxy.event.EventBus;
import com.adpumb.proxy.command.Command;

public abstract class AbstractProxyComponent implements ProxyComponent {
    private final ExecutorService executorService;
    private final EventBus eventBus;
    private static final Logger logger = LoggerManager.getInstance(AbstractProxyComponent.class.getName());

    protected AbstractProxyComponent() {
        this.executorService = Executors.newCachedThreadPool();
        this.eventBus = EventBus.getInstance();
    }

    @Override
    public ServerSocket createServerSocket(int port, String componentType) throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        logger.info(componentType + " running on port " + port);
        return serverSocket;
    }

    @Override
    public void handleAcceptError(ServerSocket serverSocket, IOException e) {
        if (serverSocket.isClosed()) {
            logger.info("Server socket closed, stopping acceptance of new connections.");
        } else {
            logger.severe("Error accepting connection: " + e.getMessage());
        }
    }

    @Override
    public void handleInterruption(String message) {
        Thread.currentThread().interrupt();
        logger.info(message);
    }

    @Override
    public void shutdown() {
        this.getExecutorService().shutdown();
    }

    protected void processCommands() {
        while (true) {
            try {
                Command command = this.getEventBus().take();
                command.execute();
            } catch (InterruptedException e) {
                this.handleInterruption("Command processing interrupted, shutting down");
                break;
            }
        }
    }

    protected ExecutorService getExecutorService() {
        return executorService;
    }

    protected EventBus getEventBus() {
        return eventBus;
    }
}
