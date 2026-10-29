package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDateTime;
import java.util.Objects;

import seedu.address.commons.util.AppUtil;

/**
 * Represents a note recorded from an interaction with a contact.
 * Guarantees: immutable; is valid as declared in {@link #isValidNote(String)}.
 */
public class InteractionNote {

    public static final int MAX_LENGTH = 2000;

    public static final String MESSAGE_CONSTRAINTS =
            "Interaction notes must not be blank, must not contain control characters, "
                    + "and must not exceed " + MAX_LENGTH + " characters.";

    private final String value;
    private final LocalDateTime createdAt;

    /**
     * Constructs an {@code InteractionNote} using the current time.
     *
     * @param note A valid interaction note.
     */
    public InteractionNote(String note) {
        this(note, LocalDateTime.now());
    }

    /**
     * Constructs an {@code InteractionNote}.
     *
     * @param note A valid interaction note.
     * @param createdAt The time the note was created.
     */
    public InteractionNote(String note, LocalDateTime createdAt) {
        requireNonNull(note);
        requireNonNull(createdAt);
        AppUtil.checkArgument(isValidNote(note), MESSAGE_CONSTRAINTS);

        value = note.trim();
        this.createdAt = createdAt;
    }

    /**
     * Returns the text of this interaction note.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the time at which this interaction note was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns true if the given string is a valid interaction note.
     */
    public static boolean isValidNote(String test) {
        return test != null
                && !isVisuallyBlank(test)
                && test.trim().length() <= MAX_LENGTH
                && test.chars().noneMatch(Character::isISOControl);
    }

    /**
     * Returns true if the text contains only whitespace or invisible spacing characters.
     */
    private static boolean isVisuallyBlank(String text) {
        return text.codePoints().allMatch(InteractionNote::isInvisibleSpacing);
    }

    /**
     * Returns true if the code point is treated as invisible spacing in an interaction note.
     */
    private static boolean isInvisibleSpacing(int codePoint) {
        return Character.isWhitespace(codePoint)
                || Character.isSpaceChar(codePoint)
                || codePoint == 0x200B;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof InteractionNote otherNote
                && value.equals(otherNote.value)
                && createdAt.equals(otherNote.createdAt));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, createdAt);
    }
}
