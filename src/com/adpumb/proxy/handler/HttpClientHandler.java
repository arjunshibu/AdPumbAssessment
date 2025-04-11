package com.adpumb.proxy.handler;

import com.adpumb.proxy.config.LoggerManager;

import java.io.*;
import java.util.logging.Logger;

public class HttpClientHandler implements RequestHandler {
    private static HttpClientHandler instance;
    private static final Logger logger = LoggerManager.getInstance(HttpClientHandler.class.getName());

    private HttpClientHandler() {
    }

    public static HttpClientHandler getInstance() {
        if (instance == null) {
            instance = new HttpClientHandler();
        }
        return instance;
    }

    @Override
    public void handleRequest(RequestContext context) throws IOException {
        try (InputStream clientInputStream = context.getClientSocket().getInputStream();
                OutputStream clientOutputStream = context.getClientSocket().getOutputStream()) {

            BufferedReader requestReader = new BufferedReader(new InputStreamReader(clientInputStream));
            String request = readRequest(requestReader);
            if (request.isEmpty()) {
                return;
            }

            sendRequestToOffshore(request, context);
            handleResponse(context, clientOutputStream);
        }
    }

    private String readRequest(BufferedReader requestReader) throws IOException {
        String requestLine = requestReader.readLine();
        if (requestLine == null) {
            return "";
        }

        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append(requestLine).append("\r\n");

        int contentLength = this.readHeaders(requestReader, requestBuilder);
        this.readBody(requestReader, requestBuilder, contentLength);

        String request = requestBuilder.toString();
        logger.info("Received request: " + requestLine);
        return request;
    }

    private int readHeaders(BufferedReader requestReader, StringBuilder requestBuilder) throws IOException {
        String line;
        int contentLength = 0;
        while ((line = requestReader.readLine()) != null && !line.isEmpty()) {
            requestBuilder.append(line).append("\r\n");
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
        }
        requestBuilder.append("\r\n");
        return contentLength;
    }

    private void readBody(BufferedReader requestReader, StringBuilder requestBuilder, int contentLength)
            throws IOException {
        if (contentLength > 0) {
            char[] body = new char[contentLength];
            int read = requestReader.read(body, 0, contentLength);
            if (read != contentLength) {
                throw new IOException("Failed to read complete request body");
            }
            requestBuilder.append(new String(body));
        }
    }

    private void sendRequestToOffshore(String request, RequestContext context) throws IOException {
        synchronized (context.getProxyOutputStream()) {
            context.getProxyOutputStream().writeInt(request.length());
            context.getProxyOutputStream().write(request.getBytes());
            context.getProxyOutputStream().flush();
        }
    }

    private void handleResponse(RequestContext context, OutputStream clientOutputStream) throws IOException {
        synchronized (context.getProxyInputStream()) {
            int len = context.getProxyInputStream().readInt();
            if (len <= 0) {
                throw new IOException("Invalid response length: " + len);
            }

            byte[] response = readResponseData(context, len);
            clientOutputStream.write(response);
            clientOutputStream.flush();
        }
    }

    private byte[] readResponseData(RequestContext context, int len) throws IOException {
        byte[] response = new byte[len];
        int totalRead = 0;
        while (totalRead < len) {
            int read = context.getProxyInputStream().read(response, totalRead, len - totalRead);
            if (read == -1) {
                throw new IOException("Unexpected end of stream while reading response");
            }
            totalRead += read;
        }
        return response;
    }
}
