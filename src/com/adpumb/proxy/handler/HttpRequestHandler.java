package com.adpumb.proxy.handler;

import com.adpumb.proxy.config.Config;
import com.adpumb.proxy.config.LoggerManager;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class HttpRequestHandler implements RequestHandler {
    private static HttpRequestHandler instance;
    private final Config config = Config.getInstance();
    private static final Logger logger = LoggerManager.getInstance(HttpRequestHandler.class.getName());
    static final Object responseLock = new Object();

    private HttpRequestHandler() {
    }

    public static HttpRequestHandler getInstance() {
        if (instance == null) {
            instance = new HttpRequestHandler();
        }
        return instance;
    }

    @Override
    public void handleRequest(RequestContext context) throws IOException {
        try {
            URLInfo urlInfo = parseRequestUrl(context.getRequest());
            try (Socket target = createTargetSocket(urlInfo)) {
                sendRequestToTarget(target, context.getRequest());
                handleTargetResponse(target, context.getResponseOutputStream());
            }
        } catch (Exception e) {
            logger.severe("Error handling HTTP request: " + e.getMessage());
            sendErrorResponse(context.getResponseOutputStream());
        }
    }

    private URLInfo parseRequestUrl(String request) throws MalformedURLException {
        String[] requestLines = request.split("\r\n");
        String[] requestLineParts = requestLines[0].split(" ");
        String urlStr = requestLineParts[1];
        URL url = new URL(urlStr);

        // Only applicable for docker compose
        // If the request is for localhost, use the target host and port
        if (url.getHost().equals("localhost") || url.getHost().equals("127.0.0.1")) {
            String host = config.getLocalHost();
            Integer port = config.getLocalPort();
            if (host != null && port != null) {
                return new URLInfo(host, port);
            }
        }

        int port = url.getPort() == -1 ? 80 : url.getPort();
        return new URLInfo(url.getHost(), port);
    }

    private Socket createTargetSocket(URLInfo urlInfo) throws IOException {
        return new Socket(urlInfo.host(), urlInfo.port());
    }

    private void sendRequestToTarget(Socket target, String request) throws IOException {
        OutputStream targetOutputStream = target.getOutputStream();
        writeRequestLineAndHeaders(targetOutputStream, request);
        writeRequestBody(targetOutputStream, request);
        targetOutputStream.flush();
    }

    private void writeRequestLineAndHeaders(OutputStream targetOutputStream, String request) throws IOException {
        String[] requestLines = request.split("\r\n");
        // Write the request line
        targetOutputStream.write((requestLines[0] + "\r\n").getBytes());

        // Write headers
        for (int i = 1; i < requestLines.length; i++) {
            if (requestLines[i].isEmpty()) {
                break; // End of headers
            }
            targetOutputStream.write((requestLines[i] + "\r\n").getBytes());
        }
        targetOutputStream.write("\r\n".getBytes());
    }

    private void writeRequestBody(OutputStream targetOutputStream, String request) throws IOException {
        int bodyStart = request.indexOf("\r\n\r\n") + 4;
        if (bodyStart < request.length()) {
            String body = request.substring(bodyStart);
            targetOutputStream.write(body.getBytes());
        }
    }

    private void handleTargetResponse(Socket target, DataOutputStream out) throws IOException {
        DataInputStream targetInputStream = new DataInputStream(target.getInputStream());
        handleResponse(targetInputStream, out);
    }

    private void handleResponse(DataInputStream targetInputStream, DataOutputStream out) throws IOException {
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        ResponseHeaders headers = readHeaders(targetInputStream);
        responseBuffer.write(headers.headerString().getBytes());

        if (headers.isChunked()) {
            handleChunkedResponse(targetInputStream, responseBuffer);
        } else if (headers.contentLength() > 0) {
            handleContentLengthResponse(targetInputStream, responseBuffer, headers.contentLength());
        } else {
            handleUnknownLengthResponse(targetInputStream, responseBuffer);
        }

        sendFinalResponse(responseBuffer, out);
    }

    private ResponseHeaders readHeaders(DataInputStream targetInputStream) throws IOException {
        StringBuilder headers = new StringBuilder();
        boolean isChunked = false;
        int contentLength = -1;

        while (true) {
            int headerByte;
            while ((headerByte = targetInputStream.read()) != -1) {
                headers.append((char) headerByte);
                if (headers.toString().endsWith("\r\n\r\n")) {
                    String headerStr = headers.toString();
                    isChunked = headerStr.toLowerCase().contains("transfer-encoding: chunked");
                    contentLength = extractContentLength(headerStr);
                    return new ResponseHeaders(headerStr, isChunked, contentLength);
                }
            }
        }
    }

    private int extractContentLength(String headerStr) {
        if (!headerStr.toLowerCase().contains("content-length:")) {
            return -1;
        }
        String[] lines = headerStr.split("\r\n");
        for (String line : lines) {
            if (line.toLowerCase().startsWith("content-length:")) {
                return Integer.parseInt(line.split(":")[1].trim());
            }
        }
        return -1;
    }

    private void handleUnknownLengthResponse(DataInputStream targetInputStream, ByteArrayOutputStream buffer)
            throws IOException {
        byte[] readBuffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = targetInputStream.read(readBuffer)) != -1) {
            buffer.write(readBuffer, 0, bytesRead);
        }
    }

    private void sendFinalResponse(ByteArrayOutputStream responseBuffer, DataOutputStream out) throws IOException {
        byte[] responseBytes = responseBuffer.toByteArray();
        synchronized (responseLock) {
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();
        }
    }

    private void handleChunkedResponse(DataInputStream targetInputStream, ByteArrayOutputStream buffer)
            throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(targetInputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty())
                continue;
            int chunkSize = Integer.parseInt(line, 16);
            if (chunkSize == 0)
                break;

            byte[] chunk = new byte[chunkSize];
            targetInputStream.readFully(chunk);
            buffer.write(chunk);
            reader.readLine();
        }
    }

    private void handleContentLengthResponse(DataInputStream targetInputStream, ByteArrayOutputStream buffer,
            int contentLength)
            throws IOException {
        byte[] content = new byte[contentLength];
        targetInputStream.readFully(content);
        buffer.write(content);
    }

    private void sendErrorResponse(DataOutputStream out) throws IOException {
        String errorResponse = "HTTP/1.1 500 Internal Server Error\r\nConnection: close\r\n\r\n";
        out.writeInt(errorResponse.length());
        out.write(errorResponse.getBytes());
        out.flush();
    }

    private record URLInfo(String host, int port) {
    }

    private record ResponseHeaders(String headerString, boolean isChunked, int contentLength) {
    }
}
