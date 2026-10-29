package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents the status of a contact in Easy-Insurance.
 */
public enum Status {
    PROSPECT,
    CLIENT,
    INACTIVE;

    public static final Status DEFAULT = PROSPECT;

    public static final String MESSAGE_CONSTRAINTS =
            "Status must be one of: PROSPECT, CLIENT, INACTIVE.";

    /**
     * Returns whether the given string represents an ASCII status, ignoring case and surrounding
     * whitespace.
     */
    public static boolean isValidStatus(String input) {
        requireNonNull(input);
        String trimmedInput = input.trim();
        // Reject lookalikes before uppercasing: dotless i would otherwise become ASCII I.
        if (!trimmedInput.matches("[a-zA-Z]+")) {
            return false;
        }

        String normalizedInput = normalize(input);
        for (Status status : values()) {
            if (status.name().equals(normalizedInput)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the status represented by the given ASCII spelling, ignoring case and surrounding whitespace.
     */
    public static Status parse(String input) {
        requireNonNull(input);
        checkArgument(isValidStatus(input), MESSAGE_CONSTRAINTS);
        return valueOf(normalize(input));
    }

    /**
     * Returns the spelling used for status validation and conversion, independent of the default locale.
     */
    private static String normalize(String input) {
        return input.trim().toUpperCase(Locale.ROOT);
    }
}
