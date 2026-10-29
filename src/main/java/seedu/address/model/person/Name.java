package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; stored in Unicode NFC form; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final int MAX_INPUT_LENGTH = 1000;

    public static final String MESSAGE_CONSTRAINTS =
            "Names must start with a letter and end with a letter or digit (which may carry combining marks). "
                    + "They may contain letters, digits, "
                    + "spaces, hyphens, apostrophes, periods, commas and slashes between characters. "
                    + "Names can be at most " + MAX_INPUT_LENGTH + " characters long.";

    /*
     * The first character must be a Unicode letter and the last a letter or decimal digit, optionally
     * followed by combining marks. The characters
     * between them may include punctuation used in names such as "s/o" and "d/o".
     */
    public static final String VALIDATION_REGEX =
            "\\p{L}(?:[\\p{L}\\p{M}\\p{Nd} '.,/-]*[\\p{L}\\p{Nd}])?\\p{M}*";

    public final String fullName;

    /**
     * Constructs a {@code Name}.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        String trimmedName = name.trim();
        checkArgument(isValidName(trimmedName), MESSAGE_CONSTRAINTS);
        fullName = Normalizer.normalize(trimmedName.replaceAll(" +", " "), Form.NFC);
    }

    /**
     * Returns true if a given string is a valid name after Unicode NFC normalization.
     */
    public static boolean isValidName(String test) {
        requireNonNull(test);
        if (test.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        String normalized = Normalizer.normalize(test, Form.NFC);
        return normalized.length() <= MAX_INPUT_LENGTH && normalized.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
