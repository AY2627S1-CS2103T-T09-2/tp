package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.Clock;
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
            "Follow-up date must be in DD-MM-YYYY format, e.g. 25-12-2026.";
    public static final String MESSAGE_NOT_A_REAL_DATE = "%1$s is not a real date.";
    public static final String MESSAGE_PAST_DATE_WARNING = "\nNote: %1$s is in the past; it was saved anyway.";
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
        return hasValidFormat(test) && isRealDate(test);
    }

    /**
     * Returns true if a given string has the DD-MM-YYYY shape, whether or not it is a real calendar date.
     */
    public static boolean hasValidFormat(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    /**
     * Returns true if a given DD-MM-YYYY string is a real calendar date, e.g. not 31-02-2026.
     */
    public static boolean isRealDate(String test) {
        try {
            return LocalDate.parse(test, FORMATTER).getYear() > 0;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Returns the warning to show when this follow-up date is already in the past.
     */
    public String getPastDateWarning() {
        return String.format(MESSAGE_PAST_DATE_WARNING, this);
    }

    /**
     * Returns true if this follow-up date is before the given date.
     */
    public boolean isBefore(LocalDate date) {
        requireNonNull(date);
        return value.isBefore(date);
    }

    /**
     * Returns true if this follow-up date is on or before the given date.
     */
    public boolean isOnOrBefore(LocalDate date) {
        requireNonNull(date);
        return !value.isAfter(date);
    }

    /**
     * Returns true if this follow-up date is before today, using the system clock and time zone.
     */
    public boolean isPast() {
        return isPast(Clock.systemDefaultZone());
    }

    /**
     * Returns true if this follow-up date is before today, as given by {@code clock}.
     */
    public boolean isPast(Clock clock) {
        requireNonNull(clock);
        return isBefore(LocalDate.now(clock));
    }

    /**
     * Returns the given date in the DD-MM-YYYY format used for follow-up dates.
     */
    public static String format(LocalDate date) {
        requireNonNull(date);
        return date.format(FORMATTER);
    }

    @Override
    public String toString() {
        return format(value);
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
