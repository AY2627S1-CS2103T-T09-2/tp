package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

public class InteractionNoteTest {

    private static final LocalDateTime FIXED_TIME =
            LocalDateTime.of(2026, 10, 26, 21, 30);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new InteractionNote(null));
    }

    @Test
    public void constructor_blankNote_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new InteractionNote("   "));
    }

    @Test
    public void constructor_noteOverMaximumLength_throwsIllegalArgumentException() {
        String tooLongNote = "a".repeat(InteractionNote.MAX_LENGTH + 1);

        assertThrows(IllegalArgumentException.class,
                () -> new InteractionNote(tooLongNote));
    }

    @Test
    public void constructor_validNote_trimsWhitespace() {
        InteractionNote note =
                new InteractionNote("  Met client  ", FIXED_TIME);

        assertEquals("Met client", note.getValue());
    }

    @Test
    public void constructor_validTimestamp_storedCorrectly() {
        InteractionNote note =
                new InteractionNote("Met client", FIXED_TIME);

        assertEquals(FIXED_TIME, note.getCreatedAt());
    }

    @Test
    public void isValidNote_null_returnsFalse() {
        assertFalse(InteractionNote.isValidNote(null));
    }

    @Test
    public void isValidNote_emptyString_returnsFalse() {
        assertFalse(InteractionNote.isValidNote(""));
    }

    @Test
    public void isValidNote_blankString_returnsFalse() {
        assertFalse(InteractionNote.isValidNote("   "));
    }

    @Test
    public void isValidNote_validNote_returnsTrue() {
        assertTrue(InteractionNote.isValidNote(
                "Discussed hospital coverage"));
    }

    @Test
    public void isValidNote_exactlyMaximumLength_returnsTrue() {
        String noteAtMaximumLength =
                "a".repeat(InteractionNote.MAX_LENGTH);

        assertTrue(InteractionNote.isValidNote(noteAtMaximumLength));
    }

    @Test
    public void isValidNote_overMaximumLength_returnsFalse() {
        String noteOverMaximumLength =
                "a".repeat(InteractionNote.MAX_LENGTH + 1);

        assertFalse(InteractionNote.isValidNote(noteOverMaximumLength));
    }

    @Test
    public void equals() {
        InteractionNote note =
                new InteractionNote("Met client", FIXED_TIME);

        InteractionNote sameNote =
                new InteractionNote("Met client", FIXED_TIME);

        InteractionNote differentText =
                new InteractionNote("Called client", FIXED_TIME);

        InteractionNote differentTimestamp =
                new InteractionNote(
                        "Met client",
                        FIXED_TIME.plusMinutes(1));

        assertEquals(note, note);
        assertEquals(note, sameNote);

        assertNotEquals(note, null);
        assertNotEquals(note, "Met client");
        assertNotEquals(note, differentText);
        assertNotEquals(note, differentTimestamp);
    }
}
