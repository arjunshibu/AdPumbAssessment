import java.util.concurrent.*;

public class EventBus {
    private static final EventBus instance = new EventBus();
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return instance;
    }

    public void publish(Command command) throws InterruptedException {
        commandQueue.put(command);
    }

    public Command take() throws InterruptedException {
        return commandQueue.take();
    }
}