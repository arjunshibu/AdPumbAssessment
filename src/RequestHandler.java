import java.io.*;

public interface RequestHandler {
    void handleRequest(RequestContext context) throws IOException;
}