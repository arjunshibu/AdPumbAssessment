public class ProxyRequestStrategy implements RequestHandlingStrategy {
    private static ProxyRequestStrategy instance;
    private RequestHandler httpHandler;
    private RequestHandler httpsHandler;

    private ProxyRequestStrategy() {
    }

    public static ProxyRequestStrategy getInstance() {
        if (instance == null) {
            instance = new ProxyRequestStrategy();
        }
        return instance;
    }

    public void setHandlers(RequestHandler httpHandler, RequestHandler httpsHandler) {
        this.httpHandler = httpHandler;
        this.httpsHandler = httpsHandler;
    }

    @Override
    public RequestHandler getHandler(String request) {
        String firstLine = request.split("\r\n")[0];
        return firstLine.toLowerCase().startsWith("connect") ? httpsHandler : httpHandler;
    }
}