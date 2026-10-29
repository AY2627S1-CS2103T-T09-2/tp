package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

public class FollowUpTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUp(null));
    }

    @Test
    public void constructor_invalidFollowUp_throwsIllegalArgumentException() {
        // EP: malformed date text.
        assertThrows(IllegalArgumentException.class, () -> new FollowUp(""));

        // EP: correctly shaped text that does not represent a real date.
        assertThrows(IllegalArgumentException.class, () -> new FollowUp("31-02-2026"));
        assertThrows(IllegalArgumentException.class, () -> new FollowUp("01-01-0000"));
    }

    @Test
    public void constructor_validFollowUp_storesDate() {
        assertEquals(LocalDate.of(2026, 9, 20), new FollowUp("20-09-2026").value);
    }

    @Test
    public void isValidFollowUp() {
        // EP: null input, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> FollowUp.isValidFollowUp(null));

        // EP: blank input rather than date text.
        // Boundary value: zero characters.
        // @@author tommyquak
        assertFalse(FollowUp.isValidFollowUp(""));
        // @@author
        assertFalse(FollowUp.isValidFollowUp(" ")); // spaces only

        // EP: nonnumeric input.
        assertFalse(FollowUp.isValidFollowUp("abc")); // non-numeric

        // EP: numeric components with incorrect widths.
        assertFalse(FollowUp.isValidFollowUp("1-9-2026")); // missing leading zeros
        assertFalse(FollowUp.isValidFollowUp("20-9-2026")); // missing leading zero in month
        assertFalse(FollowUp.isValidFollowUp("20-09-26")); // two-digit year

        // EP: components in the wrong order.
        assertFalse(FollowUp.isValidFollowUp("2026-09-20")); // year first

        // EP: separators other than hyphens.
        assertFalse(FollowUp.isValidFollowUp("20/09/2026")); // wrong separator

        // EP: otherwise valid dates with untrimmed surrounding spaces.
        assertFalse(FollowUp.isValidFollowUp(" 20-09-2026 ")); // surrounding spaces

        // EP: days beyond the selected month's length.
        assertFalse(FollowUp.isValidFollowUp("31-02-2026")); // February has no 31st
        // Boundary value: day after non-leap February ends.
        // @@author tommyquak
        assertFalse(FollowUp.isValidFollowUp("29-02-2026"));
        // @@author

        // EP: day numbers below the calendar range.
        // Boundary value: one below the minimum day.
        // @@author tommyquak
        assertFalse(FollowUp.isValidFollowUp("00-09-2026"));
        // @@author

        // EP: month numbers above the calendar range.
        // Boundary value: one above the maximum month.
        // @@author tommyquak
        assertFalse(FollowUp.isValidFollowUp("20-13-2026"));
        // @@author

        // EP: year below the supported range. Boundary value: year zero.
        assertFalse(FollowUp.isValidFollowUp("01-01-0000"));

        // EP: real calendar dates in DD-MM-YYYY form, including month/year ends and leap days.
        assertTrue(FollowUp.isValidFollowUp("20-09-2026"));
        // Boundary values: minimum day and month.
        // @@author tommyquak
        assertTrue(FollowUp.isValidFollowUp("01-01-2027"));
        // @@author
        // Boundary value: final day of a leap-year February.
        // @@author tommyquak
        assertTrue(FollowUp.isValidFollowUp("29-02-2028"));
        // @@author
        // Boundary value: final day of the year.
        // @@author tommyquak
        assertTrue(FollowUp.isValidFollowUp("31-12-2026"));
        // @@author
        assertTrue(FollowUp.isValidFollowUp("01-01-0001")); // Boundary value: minimum supported year.
    }

    @Test
    public void toString_validFollowUp_returnsOriginalFormat() {
        assertEquals("05-01-2027", new FollowUp("05-01-2027").toString());
    }

    @Test
    public void isBefore() {
        FollowUp followUp = new FollowUp("20-09-2026");

        // EP: follow-up before the comparison date. Boundary value: one day before it.
        // @@author tommyquak
        assertTrue(followUp.isBefore(LocalDate.of(2026, 9, 21)));
        // @@author

        // EP: follow-up equal to the comparison date; strict comparison excludes equality.
        // @@author tommyquak
        assertFalse(followUp.isBefore(LocalDate.of(2026, 9, 20)));
        // @@author

        // EP: follow-up after the comparison date. Boundary value: one day after it.
        // @@author tommyquak
        assertFalse(followUp.isBefore(LocalDate.of(2026, 9, 19)));
        // @@author

        // EP: null comparison date.
        assertThrows(NullPointerException.class, () -> followUp.isBefore(null));
    }

    @Test
    public void isOnOrBefore() {
        FollowUp followUp = new FollowUp("20-09-2026");

        // EP: follow-up before the cutoff. Boundary value: one day before it.
        // @@author tommyquak
        assertTrue(followUp.isOnOrBefore(LocalDate.of(2026, 9, 21)));
        // @@author

        // EP: follow-up on the cutoff; inclusive comparison includes equality.
        // @@author tommyquak
        assertTrue(followUp.isOnOrBefore(LocalDate.of(2026, 9, 20)));
        // @@author

        // EP: follow-up after the cutoff. Boundary value: one day after it.
        // @@author tommyquak
        assertFalse(followUp.isOnOrBefore(LocalDate.of(2026, 9, 19)));
        // @@author

        // EP: null cutoff.
        assertThrows(NullPointerException.class, () -> followUp.isOnOrBefore(null));
    }

    @Test
    public void format() {
        // EP: real dates, represented by single-digit day/month values that require zero padding.
        assertEquals("05-01-2027", FollowUp.format(LocalDate.of(2027, 1, 5)));

        // EP: null date.
        assertThrows(NullPointerException.class, () -> FollowUp.format(null));
    }

    @Test
    public void isPast_clockAtEndOfDay() {
        FollowUp followUp = new FollowUp("20-09-2026");

        // EP: the follow-up day itself. Boundary value: its last second is still not in the past.
        assertFalse(followUp.isPast(clockAt(LocalDateTime.of(2026, 9, 20, 23, 59, 59))));

        // EP: the day before the follow-up. Boundary value: its last second.
        assertFalse(followUp.isPast(clockAt(LocalDateTime.of(2026, 9, 19, 23, 59, 59))));
    }

    @Test
    public void isPast_clockAtStartOfDay() {
        FollowUp followUp = new FollowUp("20-09-2026");

        // EP: the day after the follow-up. Boundary value: midnight, the first instant it becomes past.
        assertTrue(followUp.isPast(clockAt(LocalDateTime.of(2026, 9, 21, 0, 0, 0))));

        // EP: the follow-up day itself. Boundary value: its first instant.
        assertFalse(followUp.isPast(clockAt(LocalDateTime.of(2026, 9, 20, 0, 0, 0))));
    }

    @Test
    public void isPast_clockAcrossYearEnd() {
        FollowUp followUp = new FollowUp("31-12-2026");

        // EP: the date changes and so does the year. Boundary values: one second either side of midnight.
        assertFalse(followUp.isPast(clockAt(LocalDateTime.of(2026, 12, 31, 23, 59, 59))));
        assertTrue(followUp.isPast(clockAt(LocalDateTime.of(2027, 1, 1, 0, 0, 0))));
    }

    @Test
    public void isPast_clockTimeZone_usesClockDate() {
        // EP: the same instant falls on different dates in different time zones.
        Instant instant = LocalDateTime.of(2026, 9, 20, 23, 0).atZone(ZONE).toInstant();
        FollowUp followUp = new FollowUp("20-09-2026");

        assertFalse(followUp.isPast(Clock.fixed(instant, ZONE))); // still 20-09-2026 in Singapore
        assertTrue(followUp.isPast(Clock.fixed(instant, ZoneId.of("Pacific/Kiritimati")))); // already 21-09-2026
    }

    @Test
    public void isPast_nullClock_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUp("20-09-2026").isPast(null));
    }

    @Test
    public void isPast_systemClock() {
        // EP: dates far enough from today that the result cannot change while the test runs.
        assertTrue(new FollowUp("01-01-2020").isPast());
        assertFalse(new FollowUp("31-12-9999").isPast());
    }

    private static Clock clockAt(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(ZONE).toInstant(), ZONE);
    }

    @Test
    public void equals() {
        FollowUp followUp = new FollowUp("20-09-2026");

        // same values -> returns true
        assertTrue(followUp.equals(new FollowUp("20-09-2026")));

        // same object -> returns true
        assertTrue(followUp.equals(followUp));

        // null -> returns false
        assertFalse(followUp.equals(null));

        // different types -> returns false
        assertFalse(followUp.equals(5.0f));

        // different values -> returns false
        assertFalse(followUp.equals(new FollowUp("21-09-2026")));
    }

    @Test
    public void hashCode_sameDate_sameHashCode() {
        assertEquals(new FollowUp("20-09-2026").hashCode(), new FollowUp("20-09-2026").hashCode());
        assertNotEquals(new FollowUp("20-09-2026").hashCode(), new FollowUp("21-09-2026").hashCode());
    }

    @Test
    public void hasValidFormat() {
        // EP: DD-MM-YYYY-shaped text; calendar validity is a separate concern.
        assertTrue(FollowUp.hasValidFormat("31-02-2026")); // right shape, impossible date

        // EP: text with components in the wrong order.
        assertFalse(FollowUp.hasValidFormat("2026-02-28")); // year first
    }

    @Test
    public void isRealDate() {
        // EP: correctly shaped text with a nonexistent calendar date.
        assertFalse(FollowUp.isRealDate("31-02-2026"));
        assertFalse(FollowUp.isRealDate("01-01-0000")); // Boundary value: year zero.

        // EP: real calendar dates. Boundary value: final day of a non-leap February.
        assertTrue(FollowUp.isRealDate("28-02-2026"));

    }

    @Test
    public void getPastDateWarning_pastDate_returnsWarningWithDate() {
        assertEquals("\nNote: 01-01-2020 is in the past; it was saved anyway.",
                new FollowUp("01-01-2020").getPastDateWarning());
    }
}
