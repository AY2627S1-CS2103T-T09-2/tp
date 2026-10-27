package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

public class FollowUpTest {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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
        assertFalse(FollowUp.isValidFollowUp("")); // Boundary value: zero characters.
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
        assertFalse(FollowUp.isValidFollowUp("29-02-2026")); // Boundary value: day after non-leap February ends.

        // EP: day numbers below the calendar range.
        assertFalse(FollowUp.isValidFollowUp("00-09-2026")); // Boundary value: one below the minimum day.

        // EP: month numbers above the calendar range.
        assertFalse(FollowUp.isValidFollowUp("20-13-2026")); // Boundary value: one above the maximum month.

        // EP: year below the supported range. Boundary value: year zero.
        assertFalse(FollowUp.isValidFollowUp("01-01-0000"));

        // EP: real calendar dates in DD-MM-YYYY form, including month/year ends and leap days.
        assertTrue(FollowUp.isValidFollowUp("20-09-2026"));
        assertTrue(FollowUp.isValidFollowUp("01-01-2027")); // Boundary values: minimum day and month.
        assertTrue(FollowUp.isValidFollowUp("29-02-2028")); // Boundary value: final day of a leap-year February.
        assertTrue(FollowUp.isValidFollowUp("31-12-2026")); // Boundary value: final day of the year.
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
        assertTrue(followUp.isBefore(LocalDate.of(2026, 9, 21)));

        // EP: follow-up equal to the comparison date; strict comparison excludes equality.
        assertFalse(followUp.isBefore(LocalDate.of(2026, 9, 20)));

        // EP: follow-up after the comparison date. Boundary value: one day after it.
        assertFalse(followUp.isBefore(LocalDate.of(2026, 9, 19)));

        // EP: null comparison date.
        assertThrows(NullPointerException.class, () -> followUp.isBefore(null));
    }

    @Test
    public void isOnOrBefore() {
        FollowUp followUp = new FollowUp("20-09-2026");

        // EP: follow-up before the cutoff. Boundary value: one day before it.
        assertTrue(followUp.isOnOrBefore(LocalDate.of(2026, 9, 21)));

        // EP: follow-up on the cutoff; inclusive comparison includes equality.
        assertTrue(followUp.isOnOrBefore(LocalDate.of(2026, 9, 20)));

        // EP: follow-up after the cutoff. Boundary value: one day after it.
        assertFalse(followUp.isOnOrBefore(LocalDate.of(2026, 9, 19)));

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
    public void isPast() {
        LocalDate today = LocalDate.now();

        // EP: dates before today. Boundary value: yesterday.
        assertTrue(new FollowUp(today.minusDays(1).format(DATE_FORMAT)).isPast());

        // EP: today, which is not yet in the past.
        assertFalse(new FollowUp(today.format(DATE_FORMAT)).isPast());

        // EP: dates after today. Boundary value: tomorrow.
        assertFalse(new FollowUp(today.plusDays(1).format(DATE_FORMAT)).isPast());
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
