package com.adpumb.proxy.handler;

import java.io.*;

public interface RequestHandler {
    void handleRequest(RequestContext context) throws IOException;
}
