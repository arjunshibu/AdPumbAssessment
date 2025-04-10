public interface RequestHandlingStrategy {
    RequestHandler getHandler(String request);
}