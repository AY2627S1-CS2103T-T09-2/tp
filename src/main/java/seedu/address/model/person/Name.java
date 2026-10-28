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

    public static final String MESSAGE_CONSTRAINTS =
            "Names must start with a letter and end with a letter or digit. They may contain letters, digits, "
                    + "spaces, hyphens, apostrophes, periods, commas and slashes between characters.";

    /*
     * The first and last characters must be Unicode letters, marks or numbers. The characters
     * between them may include punctuation used in names such as "s/o" and "d/o".
     */
    public static final String VALIDATION_REGEX =
            "\\p{L}(?:[\\p{L}\\p{M}\\p{N} '.,/-]*[\\p{L}\\p{M}\\p{N}])?";

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
        return Normalizer.normalize(test, Form.NFC).matches(VALIDATION_REGEX);
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
