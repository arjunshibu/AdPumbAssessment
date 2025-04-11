package com.adpumb.proxy.command;

import com.adpumb.proxy.handler.*;
import com.adpumb.proxy.handler.strategy.ProxyRequestHandlingStrategy;
import com.adpumb.proxy.handler.strategy.RequestHandlingStrategy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class CommandFactory {
    private static CommandFactory instance;
    private final RequestHandler clientRequestHandler;
    private final RequestHandlingStrategy requestHandlingStrategy;

    private CommandFactory() {
        RequestHandler httpRequestHandler = HttpRequestHandler.getInstance();
        RequestHandler httpsRequestHandler = HttpsRequestHandler.getInstance();
        clientRequestHandler = HttpClientHandler.getInstance();

        RequestHandlingStrategy strategy = ProxyRequestHandlingStrategy.getInstance();
        strategy.setHandlers(httpRequestHandler, httpsRequestHandler);
        requestHandlingStrategy = strategy;
    }

    public static CommandFactory getInstance() {
        if (instance == null) {
            instance = new CommandFactory();
        }
        return instance;
    }

    // Factory method for creating ServerCommand
    public Command createServerCommand(Socket socket) {
        return new ServerCommand(socket, requestHandlingStrategy);
    }

    // Factory method for creating ClientCommand
    public Command createClientCommand(Socket clientSocket, DataInputStream proxyServerInputStream,
            DataOutputStream proxyServerOutputStream) {
        return new ClientCommand(clientSocket, proxyServerInputStream, proxyServerOutputStream, clientRequestHandler);
    }
}
