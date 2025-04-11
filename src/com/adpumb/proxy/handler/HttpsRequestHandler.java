package com.adpumb.proxy.handler;

import com.adpumb.proxy.config.LoggerManager;

import java.io.*;
import java.util.logging.Logger;

public class HttpsRequestHandler implements RequestHandler {
    private static HttpsRequestHandler instance;
    private static final Logger logger = LoggerManager.getInstance(HttpsRequestHandler.class.getName());

    private HttpsRequestHandler() {
    }

    public static HttpsRequestHandler getInstance() {
        if (instance == null) {
            instance = new HttpsRequestHandler();
        }
        return instance;
    }

    @Override
    public void handleRequest(RequestContext context) throws IOException {
        String response = "HTTP/1.1 501 Not Implemented\r\nConnection: close\r\n\r\n";
        DataOutputStream out = context.getResponseOutputStream();

        out.writeInt(response.length());
        out.write(response.getBytes());
        out.flush();

        logger.info("HTTPS connection rejected");
    }
}
