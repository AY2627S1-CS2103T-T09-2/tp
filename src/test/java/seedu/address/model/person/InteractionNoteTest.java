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
        // EP: null note text violates the constructor's non-null precondition.
        assertThrows(NullPointerException.class, () ->
                new InteractionNote(null));
    }

    @Test
    public void constructor_blankNote_throwsIllegalArgumentException() {
        // EP: whitespace-only text does not contain a note.
        assertThrows(IllegalArgumentException.class, () ->
                new InteractionNote("   "));
    }

    @Test
    public void constructor_visuallyBlankNote_throwsIllegalArgumentException() {
        // EP: invisible spacing characters alone do not contain visible note text.
        assertThrows(IllegalArgumentException.class, () ->
                new InteractionNote("\u00A0"));

        assertThrows(IllegalArgumentException.class, () ->
                new InteractionNote("\u200B"));

        assertThrows(IllegalArgumentException.class, () ->
                new InteractionNote(" \u00A0\u200B "));
    }

    @Test
    public void constructor_noteOverMaximumLength_throwsIllegalArgumentException() {
        // EP: note text exceeds the allowed length.
        // Boundary value: MAX_LENGTH + 1 is the first invalid length above the limit.
        String tooLongNote = "a".repeat(InteractionNote.MAX_LENGTH + 1);

        assertThrows(IllegalArgumentException.class, () ->
                new InteractionNote(tooLongNote));
    }

    @Test
    public void constructor_validNote_trimsWhitespace() {
        // EP: valid note text surrounded by spaces is stored without the surrounding spaces.
        InteractionNote note =
                new InteractionNote("  Met client  ", FIXED_TIME);

        assertEquals("Met client", note.getValue());
    }

    @Test
    public void constructor_validTimestamp_storedCorrectly() {
        // EP: an explicitly supplied timestamp is preserved rather than replaced by the current time.
        InteractionNote note =
                new InteractionNote("Met client", FIXED_TIME);

        assertEquals(FIXED_TIME, note.getCreatedAt());
    }

    @Test
    public void isValidNote_null_returnsFalse() {
        // EP: null text is invalid, distinct from an empty or whitespace-only string.
        assertFalse(InteractionNote.isValidNote(null));
    }

    @Test
    public void isValidNote_emptyString_returnsFalse() {
        // EP: empty text is invalid; zero characters is the lower length boundary.
        assertFalse(InteractionNote.isValidNote(""));
    }

    @Test
    public void isValidNote_blankString_returnsFalse() {
        // EP: nonempty whitespace-only text is still blank and invalid.
        assertFalse(InteractionNote.isValidNote("   "));
    }

    @Test
    public void isValidNote_visuallyBlankString_returnsFalse() {
        // EP: NBSP and zero-width spaces are invisible and do not count as note content.
        assertFalse(InteractionNote.isValidNote("\u00A0"));
        assertFalse(InteractionNote.isValidNote("\u200B"));
        assertFalse(InteractionNote.isValidNote(" \u00A0\u200B "));
    }

    @Test
    public void isValidNote_visibleTextWithInvisibleSpacing_returnsTrue() {
        // EP: invisible spacing is allowed when the note still contains visible text.
        assertTrue(InteractionNote.isValidNote("\u200BMet client\u00A0"));
    }

    @Test
    public void isValidNote_validNote_returnsTrue() {
        // EP: nonblank text shorter than the maximum length is valid.
        assertTrue(InteractionNote.isValidNote(
                "Discussed hospital coverage"));
    }

    @Test
    public void isValidNote_exactlyMaximumLength_returnsTrue() {
        // EP: nonblank text within the length limit is valid.
        // Boundary value: MAX_LENGTH is the largest allowed length after trimming.
        String noteAtMaximumLength =
                "a".repeat(InteractionNote.MAX_LENGTH);

        assertTrue(InteractionNote.isValidNote(noteAtMaximumLength));
    }

    @Test
    public void isValidNote_overMaximumLength_returnsFalse() {
        // EP: text longer than the maximum is invalid.
        // Boundary value: MAX_LENGTH + 1 is the first invalid length above the limit.
        String noteOverMaximumLength =
                "a".repeat(InteractionNote.MAX_LENGTH + 1);

        assertFalse(InteractionNote.isValidNote(noteOverMaximumLength));
    }

    @Test
    public void isValidNote_exactlyMaximumUnicodeCharacters_returnsTrue() {
        // Boundary value: supplementary Unicode characters count as one code point each.
        String noteAtMaximumLength =
                "😀".repeat(InteractionNote.MAX_LENGTH);

        assertEquals(
                InteractionNote.MAX_LENGTH * 2,
                noteAtMaximumLength.length());

        assertEquals(
                InteractionNote.MAX_LENGTH,
                noteAtMaximumLength.codePointCount(
                        0,
                        noteAtMaximumLength.length()));

        assertTrue(InteractionNote.isValidNote(noteAtMaximumLength));
    }

    @Test
    public void isValidNote_overMaximumUnicodeCharacters_returnsFalse() {
        // Boundary value: MAX_LENGTH + 1 Unicode code points is invalid.
        String noteOverMaximumLength =
                "😀".repeat(InteractionNote.MAX_LENGTH + 1);

        assertEquals(
                InteractionNote.MAX_LENGTH + 1,
                noteOverMaximumLength.codePointCount(
                        0,
                        noteOverMaximumLength.length()));

        assertFalse(InteractionNote.isValidNote(noteOverMaximumLength));
    }
  
    @Test
    public void constructor_controlCharacter_throwsIllegalArgumentException() {
        // EP: control characters are not allowed in interaction note text.
        assertThrows(IllegalArgumentException.class, () ->
                new InteractionNote("Called\u0000client"));
    }

    @Test
    public void isValidNote_controlCharacters_returnsFalse() {
        // EP: notes containing ISO control characters are invalid.
        assertFalse(InteractionNote.isValidNote("Called\u0000client"));
        assertFalse(InteractionNote.isValidNote("Called\nclient"));
        assertFalse(InteractionNote.isValidNote("Called\tclient"));
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

        // EP: the same object or matching text and timestamp identifies an equal note.
        assertEquals(note, note);
        assertEquals(note, sameNote);

        // EP: null and objects of other types cannot be equal notes.
        assertNotEquals(note, null);
        assertNotEquals(note, "Met client");

        // EP: a difference in either text or timestamp makes notes unequal.
        assertNotEquals(note, differentText);
        assertNotEquals(note, differentTimestamp);
    }
}
