package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Tests command-history key selection without requiring a graphical environment.
 */
public class CommandBoxTest {

    @Test
    public void isHistoryKey_focusedPlainArrows_returnsTrue() {
        // EP: unmodified Up and Down presses belong to history when the command input has focus.
        for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN)) {
            assertTrue(CommandBox.isHistoryKey(keyPressed(code), true));
        }
    }

    @Test
    public void isHistoryKey_unfocusedArrows_returnsFalse() {
        // EP: arrows belong to the focused control, such as the contact list, instead of command history.
        for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN)) {
            assertFalse(CommandBox.isHistoryKey(keyPressed(code), false));
        }
    }

    @Test
    public void isHistoryKey_modifiedArrows_returnsFalse() {
        // EP: selection and platform shortcuts retain their normal behavior for every modifier.
        for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN)) {
            List<KeyEvent> modifiedArrows = List.of(
                    new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, true, false, false, false),
                    new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, true, false, false),
                    new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, true, false),
                    new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, true));
            for (KeyEvent event : modifiedArrows) {
                assertFalse(CommandBox.isHistoryKey(event, true));
            }
        }
    }

    @Test
    public void isHistoryKey_otherKeys_returnsFalse() {
        // EP: caret movement, editing, submission and other shortcuts are not history navigation.
        for (KeyCode code : List.of(KeyCode.LEFT, KeyCode.RIGHT, KeyCode.HOME, KeyCode.END,
                KeyCode.A, KeyCode.C, KeyCode.V, KeyCode.BACK_SPACE, KeyCode.DELETE,
                KeyCode.ENTER, KeyCode.TAB, KeyCode.F1)) {
            assertFalse(CommandBox.isHistoryKey(keyPressed(code), true));
        }
    }

    @Test
    public void isHistoryKey_releasedOrTypedEvent_returnsFalse() {
        // EP: key release and character events must not trigger a second history movement.
        for (KeyCode code : List.of(KeyCode.UP, KeyCode.DOWN)) {
            KeyEvent released = new KeyEvent(KeyEvent.KEY_RELEASED, "", "", code,
                    false, false, false, false);
            assertFalse(CommandBox.isHistoryKey(released, true));
        }
        KeyEvent typed = new KeyEvent(KeyEvent.KEY_TYPED, "a", "", KeyCode.UNDEFINED,
                false, false, false, false);
        assertFalse(CommandBox.isHistoryKey(typed, true));
    }

    @Test
    public void isBlankCommand_whitespaceOnly_returnsTrue() {
        assertTrue(CommandBox.isBlankCommand("   "));
        assertTrue(CommandBox.isBlankCommand("\u2028"));
        assertTrue(CommandBox.isBlankCommand("\u2029"));
        assertTrue(CommandBox.isBlankCommand(" \u2028\u2029 "));
    }

    @Test
    public void isBlankCommand_containsVisibleText_returnsFalse() {
        assertFalse(CommandBox.isBlankCommand("help"));
        assertFalse(CommandBox.isBlankCommand("  help  "));
    }

    private KeyEvent keyPressed(KeyCode code) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false);
    }
}
