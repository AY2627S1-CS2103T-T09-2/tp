package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;

/**
 * Captures one class's logs, including FINE records, and restores its logger when closed.
 */
public class LogCapture extends Handler implements AutoCloseable {

    private static final Logger parentLogger = LogsCenter.getLogger(LogCapture.class).getParent();

    private final Logger logger;
    private final Level previousLevel;
    private final boolean previousUseParentHandlers;
    private final List<LogRecord> records = new ArrayList<>();

    /**
     * Captures records from {@code sourceClass} without replacing existing handlers.
     */
    public LogCapture(Class<?> sourceClass) {
        logger = Logger.getLogger(parentLogger.getName() + "." + sourceClass.getSimpleName());
        previousLevel = logger.getLevel();
        previousUseParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(true);
        setLevel(Level.ALL);
        setFilter(record -> logger.getName().equals(record.getLoggerName()));
        // LogsCenter removes a class logger's handlers when that class first initializes.
        parentLogger.addHandler(this);
    }

    @Override
    public void publish(LogRecord record) {
        if (isLoggable(record)) {
            records.add(record);
        }
    }

    @Override
    public void flush() {
        // Records are kept in memory.
    }

    @Override
    public void close() {
        parentLogger.removeHandler(this);
        logger.setLevel(previousLevel);
        logger.setUseParentHandlers(previousUseParentHandlers);
    }

    public List<LogRecord> getRecords() {
        return List.copyOf(records);
    }

    /**
     * Checks that exactly one record has the expected level and diagnostic context.
     */
    public void assertSingleLog(Level expectedLevel, String... messageParts) {
        assertEquals(1, records.size(), "Expected exactly one diagnostic record");
        LogRecord record = records.getFirst();
        assertEquals(expectedLevel, record.getLevel());
        for (String part : messageParts) {
            assertTrue(record.getMessage().contains(part), "Missing diagnostic context: " + part);
        }
    }
}
