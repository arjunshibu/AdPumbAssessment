import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class LoggerManager {
    private static LoggerManager instance;

    private LoggerManager() {
        configureRootLogger();
    }

    public static Logger getInstance(String name) {
        if (instance == null) {
            instance = new LoggerManager();
        }
        return instance.getLogger(name);
    }

    private void configureRootLogger() {
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        if (handlers[0] instanceof ConsoleHandler) {
            rootLogger.removeHandler(handlers[0]);
        }

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new CustomStyleFormatter());
        rootLogger.addHandler(consoleHandler);
    }

    private Logger getLogger(String name) {
        return Logger.getLogger(name);
    }
}