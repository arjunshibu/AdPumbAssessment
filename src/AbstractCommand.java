import java.io.IOException;

public abstract class AbstractCommand implements Command {
    @Override
    public final void execute() {
        try {
            processRequests();
        } catch (Exception e) {
            handleError(e);
        } finally {
            cleanup();
        }
    }

    protected abstract void processRequests() throws IOException;
    protected abstract void handleError(Exception e);
    protected abstract void cleanup();
}