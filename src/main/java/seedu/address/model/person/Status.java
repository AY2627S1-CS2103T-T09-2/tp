package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents the relationship status of a contact in Easy-Insurance.
 */
public enum Status {
    PROSPECT,
    CLIENT,
    INACTIVE;

    public static final String MESSAGE_CONSTRAINTS =
            "Status must be one of: PROSPECT, CLIENT, INACTIVE.";

    /**
     * Returns whether the given string represents a status, ignoring case and surrounding
     * whitespace.
     */
    public static boolean isValidStatus(String input) {
        requireNonNull(input);
        String normalizedInput = input.trim();
        for (Status status : values()) {
            if (status.name().equalsIgnoreCase(normalizedInput)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the status represented by the given string, ignoring case and surrounding whitespace.
     */
    public static Status parse(String input) {
        requireNonNull(input);
        checkArgument(isValidStatus(input), MESSAGE_CONSTRAINTS);
        return valueOf(input.trim().toUpperCase(Locale.ROOT));
    }
}
