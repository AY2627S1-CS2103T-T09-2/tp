package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seedu.address.testutil.LogCapture;

/**
 * Tests Help keyboard handling, the User Guide link and clipboard outcomes without a graphical environment.
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

    @Test
    public void handleCloseKey_plainEscape_closesHelpAndConsumesEvent() {
        // EP: a plain Escape press closes Help exactly once and must not reach another key handler.
        AtomicInteger closeCount = new AtomicInteger();
        KeyEvent event = keyPressed(KeyCode.ESCAPE);

        HelpWindow.handleCloseKey(event, closeCount::incrementAndGet);

        assertEquals(1, closeCount.get());
        assertTrue(event.isConsumed());
    }

    @Test
    public void handleCloseKey_modifiedEscape_preservesPlatformShortcuts() {
        // EP: each modifier makes Escape a different shortcut; none should trigger Help's close action.
        List<KeyEvent> events = List.of(
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, true, false, false, false),
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, true, false, false),
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, true, false),
                new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE, false, false, false, true));
        for (KeyEvent event : events) {
            assertCloseKeyIgnored(event);
        }
    }

    @Test
    public void handleCloseKey_otherKeys_preservesScrollingAndFocusTraversal() {
        // EP: scrolling keys, Tab, button activation and unrelated keys keep their normal control behavior.
        for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN, KeyCode.PAGE_UP, KeyCode.PAGE_DOWN,
                KeyCode.HOME, KeyCode.END, KeyCode.TAB, KeyCode.SPACE, KeyCode.ENTER, KeyCode.A, KeyCode.F1)) {
            assertCloseKeyIgnored(keyPressed(code));
        }
        assertCloseKeyIgnored(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.TAB,
                true, false, false, false));
    }

    @Test
    public void handleCloseKey_releasedOrTypedEvent_doesNotCloseAgain() {
        // EP: release and character events do not repeat the action performed by a key press.
        assertCloseKeyIgnored(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.ESCAPE,
                false, false, false, false));
        assertCloseKeyIgnored(new KeyEvent(KeyEvent.KEY_TYPED, "\u001b", "", KeyCode.UNDEFINED,
                false, false, false, false));
    }

    private void assertCloseKeyIgnored(KeyEvent event) {
        AtomicInteger closeCount = new AtomicInteger();

        HelpWindow.handleCloseKey(event, closeCount::incrementAndGet);

        assertEquals(0, closeCount.get());
        assertFalse(event.isConsumed());
    }

    private KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
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
