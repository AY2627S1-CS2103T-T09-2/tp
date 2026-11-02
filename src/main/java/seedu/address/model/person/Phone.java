package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final int MAX_LENGTH = 30;

    public static final String MESSAGE_CONSTRAINTS =
            "Phone numbers must have at least 3 digits and at most " + MAX_LENGTH + " characters, may start "
                    + "with +, and may separate digits with a single space or dash, e.g. +65 9123-4567.";

    // The lookahead checks for 3 digits; the rest uses a character class, which the regex engine
    // matches in a loop rather than by recursion, so very long input cannot overflow the stack.
    // The negative lookahead rejects consecutive spaces or dashes between digits.
    public static final String VALIDATION_REGEX =
            "\\+?(?=(?:[ -]*\\d){3})(?![\\d -]*[ -]{2})\\d[\\d -]*\\d";
    public final String value;

    /**
     * Constructs a {@code Phone}.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone;
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
