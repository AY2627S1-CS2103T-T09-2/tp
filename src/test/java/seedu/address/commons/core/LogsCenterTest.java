package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests logger initialization in fresh JVMs, before any application class has obtained a logger.
 */
public class LogsCenterTest {

    private static final String PROBE_MESSAGE = "Logging startup probe completed.";

    @TempDir
    public Path testFolder;

    @Test
    public void initialization_writableLogFile_logsToConsoleAndFile() throws Exception {
        String output = runStartupProbe(true);

        assertTrue(output.contains(PROBE_MESSAGE), output);
        assertTrue(Files.readString(testFolder.resolve("easyinsurance.log.0")).contains(PROBE_MESSAGE));
    }

    @Test
    public void initialization_blockedLogFile_continuesWithConsoleLogging() throws Exception {
        Files.createDirectory(testFolder.resolve("easyinsurance.log.0"));

        String output = runStartupProbe(false);

        assertTrue(output.contains("Continuing with console logging"), output);
        assertTrue(output.contains(PROBE_MESSAGE), output);
        assertTrue(Files.isDirectory(testFolder.resolve("easyinsurance.log.0")));
    }

    private String runStartupProbe(boolean expectFileHandler) throws Exception {
        String executable = System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java";
        String java = Path.of(System.getProperty("java.home"), "bin", executable).toString();
        String mainClasses = Path.of(LogsCenter.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI()).toString();
        String testClasses = Path.of(LogsCenterTest.class.getProtectionDomain().getCodeSource()
                .getLocation().toURI()).toString();
        Path outputFile = testFolder.resolve("startup-output.txt");
        Process process = new ProcessBuilder(java, "-cp", mainClasses + File.pathSeparator + testClasses,
                StartupProbe.class.getName(), Boolean.toString(expectFileHandler))
                .directory(testFolder.toFile())
                .redirectErrorStream(true)
                .redirectOutput(outputFile.toFile())
                .start();

        try {
            assertTrue(process.waitFor(20, TimeUnit.SECONDS), "Logging startup probe timed out");
            String output = Files.readString(outputFile);
            assertEquals(0, process.exitValue(), output);
            return output;
        } finally {
            process.destroyForcibly();
        }
    }

    /**
     * Entry point for a child JVM that checks the installed handlers and emits a real log message.
     */
    public static class StartupProbe {
        public static void main(String[] args) {
            Logger logger = LogsCenter.getLogger(StartupProbe.class);
            Handler[] handlers = logger.getParent().getHandlers();
            boolean hasConsoleHandler = Arrays.stream(handlers).anyMatch(ConsoleHandler.class::isInstance);
            boolean hasFileHandler = Arrays.stream(handlers).anyMatch(FileHandler.class::isInstance);
            if (!hasConsoleHandler || hasFileHandler != Boolean.parseBoolean(args[0])) {
                throw new AssertionError("Unexpected logging handlers after initialization");
            }
            logger.info(PROBE_MESSAGE);
        }
    }
}
