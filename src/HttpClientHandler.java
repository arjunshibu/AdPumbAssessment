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
            String requestLine = requestReader.readLine();
            if (requestLine == null) {
                return;
            }

            // Build request
            StringBuilder requestBuilder = new StringBuilder();
            requestBuilder.append(requestLine).append("\r\n");

            String line;
            while ((line = requestReader.readLine()) != null && !line.isEmpty()) {
                requestBuilder.append(line).append("\r\n");
            }
            requestBuilder.append("\r\n");

            String request = requestBuilder.toString();
            logger.info("Received request: " + requestLine);

            // Send to offshore
            synchronized (context.getProxyOutputStream()) {
                context.getProxyOutputStream().writeInt(request.length());
                context.getProxyOutputStream().write(request.getBytes());
                context.getProxyOutputStream().flush();
            }

            // Handle response
            synchronized (context.getProxyInputStream()) {
                int len = context.getProxyInputStream().readInt();
                if (len <= 0) {
                    throw new IOException("Invalid response length: " + len);
                }

                byte[] response = new byte[len];
                int totalRead = 0;
                while (totalRead < len) {
                    int read = context.getProxyInputStream().read(response, totalRead, len - totalRead);
                    if (read == -1) {
                        throw new IOException("Unexpected end of stream while reading response");
                    }
                    totalRead += read;
                }

                clientOutputStream.write(response);
                clientOutputStream.flush();
            }
        }
    }
}