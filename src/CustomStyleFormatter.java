import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class CustomStyleFormatter extends Formatter {
    private static final String format = "%2$tm/%2$td/%2$tY, %2$tI:%2$tM:%2$tS %2$Tp - %3$s %n";

    @Override
    public synchronized String format(LogRecord lr) {
        return String.format(format,
                Thread.currentThread().threadId(),
                new java.util.Date(lr.getMillis()),
                lr.getMessage());
    }
}