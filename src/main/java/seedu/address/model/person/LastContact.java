package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;

import seedu.address.commons.util.AppUtil;

/**
 * Represents the date a contact was last contacted.
 * Guarantees: immutable; is valid as declared in {@link #isValidLastContact(String)}.
 */
public class LastContact {

    public static final String MESSAGE_CONSTRAINTS =
            "Last contact date must be a valid date in DD-MM-YYYY format.";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    private final LocalDate date;

    /**
     * Constructs a {@code LastContact}.
     *
     * @param lastContact A valid last contact date in DD-MM-YYYY format.
     */
    public LastContact(String lastContact) {
        requireNonNull(lastContact);
        AppUtil.checkArgument(
                isValidLastContact(lastContact),
                MESSAGE_CONSTRAINTS);

        date = LocalDate.parse(lastContact, FORMATTER);
    }

    /**
     * Returns true if the given string is a valid last contact date.
     */
    public static boolean isValidLastContact(String test) {
        if (test == null) {
            return false;
        }

        try {
            LocalDate.parse(test, FORMATTER);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    @Override
    public String toString() {
        return date.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof LastContact otherLastContact
                && date.equals(otherLastContact.date));
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}
