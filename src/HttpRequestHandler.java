import java.io.*;
import java.net.*;
import java.util.logging.Logger;

public class HttpRequestHandler implements RequestHandler {
    private static HttpRequestHandler instance;
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
            String[] requestLines = context.getRequest().split("\r\n");
            String[] requestLineParts = requestLines[0].split(" ");
            String urlStr = requestLineParts[1];
            URL url = new URL(urlStr);
            int port = url.getPort() == -1 ? 80 : url.getPort();

            try (Socket target = new Socket(url.getHost(), port)) {
                OutputStream targetOutputStream = target.getOutputStream();
                DataInputStream targetInputStream = new DataInputStream(target.getInputStream());

                targetOutputStream.write(context.getRequest().getBytes());
                targetOutputStream.flush();

                handleResponse(targetInputStream, context.getResponseOutputStream());
            }
        } catch (Exception e) {
            logger.severe("Error handling HTTP request: " + e.getMessage());
            sendErrorResponse(context.getResponseOutputStream());
        }
    }

    private void handleResponse(DataInputStream targetInputStream, DataOutputStream out) throws IOException {
        ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int bytesRead;
        boolean isChunked = false;
        int contentLength = -1;
        boolean headersComplete = false;
        StringBuilder headers = new StringBuilder();

        // First read headers
        while (!headersComplete) {
            int headerByte;
            while ((headerByte = targetInputStream.read()) != -1) {
                headers.append((char) headerByte);
                if (headers.toString().endsWith("\r\n\r\n")) {
                    headersComplete = true;
                    break;
                }
            }

            String headerStr = headers.toString();
            if (headerStr.toLowerCase().contains("transfer-encoding: chunked")) {
                isChunked = true;
            } else if (headerStr.toLowerCase().contains("content-length:")) {
                String[] lines = headerStr.split("\r\n");
                for (String line : lines) {
                    if (line.toLowerCase().startsWith("content-length:")) {
                        contentLength = Integer.parseInt(line.split(":")[1].trim());
                        break;
                    }
                }
            }
        }

        responseBuffer.write(headers.toString().getBytes());

        if (isChunked) {
            handleChunkedResponse(targetInputStream, responseBuffer);
        } else if (contentLength > 0) {
            handleContentLengthResponse(targetInputStream, responseBuffer, contentLength);
        } else {
            while ((bytesRead = targetInputStream.read(buffer)) != -1) {
                responseBuffer.write(buffer, 0, bytesRead);
            }
        }

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
            reader.readLine(); // Skip the \r\n after the chunk
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
}