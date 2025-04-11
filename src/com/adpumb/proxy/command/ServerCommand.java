package com.adpumb.proxy.command;

import com.adpumb.proxy.config.LoggerManager;
import com.adpumb.proxy.handler.RequestContext;
import com.adpumb.proxy.handler.RequestHandler;
import com.adpumb.proxy.handler.strategy.RequestHandlingStrategy;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class ServerCommand extends AbstractCommand {
    private final Socket proxySocket;
    private static final Logger logger = LoggerManager.getInstance(ServerCommand.class.getName());
    private final RequestHandlingStrategy requestHandlingStrategy;

    public ServerCommand(Socket proxySocket, RequestHandlingStrategy requestHandlingStrategy) {
        this.proxySocket = proxySocket;
        this.requestHandlingStrategy = requestHandlingStrategy;
    }

    @Override
    protected void processRequests() throws IOException {
        try (DataInputStream inputStream = new DataInputStream(proxySocket.getInputStream());
                DataOutputStream outputStream = new DataOutputStream(proxySocket.getOutputStream())) {

            while (!proxySocket.isClosed()) {
                String request = readRequest(inputStream);
                if (request == null) {
                    logger.info("Client disconnected");
                    break;
                }

                String firstLine = request.split("\r\n")[0];
                logger.info("Received request: " + firstLine);

                RequestContext context = new RequestContext.Builder()
                        .withRequest(request)
                        .withResponseOutputStream(outputStream)
                        .build();

                RequestHandler handler = requestHandlingStrategy.getHandler(request);
                handler.handleRequest(context);
            }
        }
    }

    private String readRequest(DataInputStream inputStream) throws IOException {
        int requestLength;
        try {
            requestLength = inputStream.readInt();
        } catch (EOFException eof) {
            return null;
        }

        byte[] requestBytes = new byte[requestLength];
        inputStream.readFully(requestBytes);
        return new String(requestBytes);
    }

    @Override
    protected void handleError(Exception e) {
        logger.severe("Error handling server: " + e.getMessage());
    }

    @Override
    protected void cleanup() {
        try {
            if (proxySocket != null && !proxySocket.isClosed()) {
                proxySocket.close();
            }
        } catch (IOException e) {
            logger.severe("Error closing server socket: " + e.getMessage());
        }
    }
}
