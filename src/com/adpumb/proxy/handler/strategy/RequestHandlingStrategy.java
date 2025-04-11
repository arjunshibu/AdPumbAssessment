package com.adpumb.proxy.handler.strategy;

import com.adpumb.proxy.handler.RequestHandler;

public interface RequestHandlingStrategy {
    RequestHandler getHandler(String request);

    void setHandlers(RequestHandler httpHandler, RequestHandler httpsHandler);
}
