package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Represents the date on which a Person should next be followed up with.
 * Guarantees: immutable; is valid as declared in {@link #isValidFollowUp(String)}
 */
public class FollowUp {

    public static final String MESSAGE_CONSTRAINTS =
            "Follow-up date must be a valid date in DD-MM-YYYY format.";
    public static final String VALIDATION_REGEX = "\\d{2}-\\d{2}-\\d{4}";

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    public final LocalDate value;

    /**
     * Constructs a {@code FollowUp}.
     *
     * @param date A valid follow-up date in DD-MM-YYYY format.
     */
    public FollowUp(String date) {
        requireNonNull(date);
        checkArgument(isValidFollowUp(date), MESSAGE_CONSTRAINTS);
        value = LocalDate.parse(date, FORMATTER);
    }

    /**
     * Returns true if a given string is a valid follow-up date in DD-MM-YYYY format.
     */
    public static boolean isValidFollowUp(String test) {
        if (!test.matches(VALIDATION_REGEX)) {
            return false;
        }
        try {
            LocalDate.parse(test, FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns true if this follow-up date is before the given date.
     */
    public boolean isBefore(LocalDate date) {
        requireNonNull(date);
        return value.isBefore(date);
    }

    /**
     * Returns true if this follow-up date is before today.
     */
    public boolean isPast() {
        return isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return value.format(FORMATTER);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FollowUp otherFollowUp)) {
            return false;
        }

        return value.equals(otherFollowUp.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
