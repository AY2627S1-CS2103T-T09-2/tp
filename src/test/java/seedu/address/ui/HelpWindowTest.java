package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.LogCapture;

/**
 * Tests the User Guide link and clipboard outcomes without requiring a graphical environment.
 */
public class HelpWindowTest {

    @Test
    public void userGuideUrl_pointsToEasyInsuranceUserGuide() {
        String expectedUrl =
                "https://ay2627s1-cs2103t-t09-2.github.io/tp/UserGuide.html";

        assertEquals(expectedUrl, HelpWindow.USERGUIDE_URL);
    }

    @Test
    public void copyUserGuideUrl_clipboardAcceptsContent_logsSuccess() {
        // EP: the clipboard accepts the URL and reports success.
        assertClipboardOutcome(true, Level.FINE, "Copied User Guide URL");
    }

    @Test
    public void copyUserGuideUrl_clipboardRejectsContent_logsWarning() {
        // EP: the clipboard returns false, representing an expected copy failure.
        assertClipboardOutcome(false, Level.WARNING, "Could not copy the User Guide URL");
    }

    @Test
    public void copyUserGuideUrl_clipboardThrows_doesNotHideProgrammingError() {
        // EP: the clipboard throws an unexpected exception instead of returning an outcome.
        try (LogCapture logs = new LogCapture(HelpWindow.class)) {
            assertThrows(IllegalStateException.class, "Clipboard thread error", () -> {
                HelpWindow.copyUserGuideUrl(content -> {
                    throw new IllegalStateException("Clipboard thread error");
                });
            });
            assertTrue(logs.getRecords().isEmpty());
        }
    }

    private void assertClipboardOutcome(boolean copied, Level expectedLevel, String expectedContext) {
        AtomicInteger attempts = new AtomicInteger();
        try (LogCapture logs = new LogCapture(HelpWindow.class)) {
            HelpWindow.copyUserGuideUrl(content -> {
                attempts.incrementAndGet();
                assertEquals(HelpWindow.USERGUIDE_URL, content.getString());
                assertEquals(1, content.size());
                return copied;
            });
            assertEquals(1, attempts.get());
            logs.assertSingleLog(expectedLevel, expectedContext);
        }
    }
}
