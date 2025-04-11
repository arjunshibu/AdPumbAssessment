package com.adpumb.proxy.core;

import java.io.IOException;
import java.net.ServerSocket;

public interface ProxyComponent {
    // Starts the proxy component
    void start();

    // Initiates the connection acceptor
    void startConnectionAcceptor(ServerSocket serverSocket, Object... args);

    // Handles a new incoming connection
    void handleNewConnection(ServerSocket serverSocket, Object... args)
            throws IOException, InterruptedException;

    // Creates a server socket for the proxy component
    ServerSocket createServerSocket(int port, String componentType) throws IOException;

    // Handles errors during connection acceptance
    void handleAcceptError(ServerSocket serverSocket, IOException e);

    // Handles interruption of the proxy component
    void handleInterruption(String message);

    // Shuts down the proxy component
    void shutdown();
}
