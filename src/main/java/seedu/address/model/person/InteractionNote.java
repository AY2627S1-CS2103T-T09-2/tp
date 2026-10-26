package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.AppUtil;

/**
 * Represents a note recorded from an interaction with a contact.
 * Guarantees: immutable; is valid as declared in {@link #isValidNote(String)}.
 */
public class InteractionNote {

    public static final String MESSAGE_CONSTRAINTS =
            "Interaction notes must not be blank.";

    public final String value;

    /**
     * Constructs an {@code InteractionNote}.
     *
     * @param note A valid interaction note.
     */
    public InteractionNote(String note) {
        requireNonNull(note);
        AppUtil.checkArgument(isValidNote(note), MESSAGE_CONSTRAINTS);
        value = note.trim();
    }

    /**
     * Returns true if the given string is a valid interaction note.
     */
    public static boolean isValidNote(String test) {
        return test != null && !test.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof InteractionNote otherNote
                && value.equals(otherNote.value));
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
