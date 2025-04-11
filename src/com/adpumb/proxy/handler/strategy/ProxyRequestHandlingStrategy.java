package com.adpumb.proxy.handler.strategy;

import com.adpumb.proxy.handler.RequestHandler;

public class ProxyRequestHandlingStrategy implements RequestHandlingStrategy {
    private static ProxyRequestHandlingStrategy instance;
    private RequestHandler httpHandler;
    private RequestHandler httpsHandler;

    private ProxyRequestHandlingStrategy() {
    }

    public static ProxyRequestHandlingStrategy getInstance() {
        if (instance == null) {
            instance = new ProxyRequestHandlingStrategy();
        }
        return instance;
    }

    @Override
    public RequestHandler getHandler(String request) {
        String firstLine = request.split("\r\n")[0];
        return firstLine.toLowerCase().startsWith("connect") ? httpsHandler : httpHandler;
    }

    @Override
    public void setHandlers(RequestHandler httpHandler, RequestHandler httpsHandler) {
        this.httpHandler = httpHandler;
        this.httpsHandler = httpsHandler;
    }
}
