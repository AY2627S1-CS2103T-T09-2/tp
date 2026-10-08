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
        assertThrows(IllegalArgumentException.class, () -> new FollowUp(""));
        assertThrows(IllegalArgumentException.class, () -> new FollowUp("31-02-2026"));
    }

    @Test
    public void constructor_validFollowUp_storesDate() {
        assertEquals(LocalDate.of(2026, 9, 20), new FollowUp("20-09-2026").value);
    }

    @Test
    public void isValidFollowUp() {
        // null date
        assertThrows(NullPointerException.class, () -> FollowUp.isValidFollowUp(null));

        // invalid format
        assertFalse(FollowUp.isValidFollowUp("")); // empty string
        assertFalse(FollowUp.isValidFollowUp(" ")); // spaces only
        assertFalse(FollowUp.isValidFollowUp("abc")); // non-numeric
        assertFalse(FollowUp.isValidFollowUp("1-9-2026")); // missing leading zeros
        assertFalse(FollowUp.isValidFollowUp("20-9-2026")); // missing leading zero in month
        assertFalse(FollowUp.isValidFollowUp("20-09-26")); // two-digit year
        assertFalse(FollowUp.isValidFollowUp("2026-09-20")); // year first
        assertFalse(FollowUp.isValidFollowUp("20/09/2026")); // wrong separator
        assertFalse(FollowUp.isValidFollowUp(" 20-09-2026 ")); // surrounding spaces

        // invalid calendar dates
        assertFalse(FollowUp.isValidFollowUp("31-02-2026")); // February has no 31st
        assertFalse(FollowUp.isValidFollowUp("29-02-2026")); // not a leap year
        assertFalse(FollowUp.isValidFollowUp("00-09-2026")); // day zero
        assertFalse(FollowUp.isValidFollowUp("20-13-2026")); // month 13

        // valid dates
        assertTrue(FollowUp.isValidFollowUp("20-09-2026"));
        assertTrue(FollowUp.isValidFollowUp("01-01-2027"));
        assertTrue(FollowUp.isValidFollowUp("29-02-2028")); // leap year
        assertTrue(FollowUp.isValidFollowUp("31-12-2026"));
    }

    @Test
    public void toString_validFollowUp_returnsOriginalFormat() {
        assertEquals("05-01-2027", new FollowUp("05-01-2027").toString());
    }

    @Test
    public void isBefore() {
        FollowUp followUp = new FollowUp("20-09-2026");

        assertTrue(followUp.isBefore(LocalDate.of(2026, 9, 21))); // day after
        assertFalse(followUp.isBefore(LocalDate.of(2026, 9, 20))); // same day
        assertFalse(followUp.isBefore(LocalDate.of(2026, 9, 19))); // day before
        assertThrows(NullPointerException.class, () -> followUp.isBefore(null));
    }

    @Test
    public void isPast() {
        LocalDate today = LocalDate.now();

        assertTrue(new FollowUp(today.minusDays(1).format(DATE_FORMAT)).isPast()); // yesterday
        assertFalse(new FollowUp(today.format(DATE_FORMAT)).isPast()); // today
        assertFalse(new FollowUp(today.plusDays(1).format(DATE_FORMAT)).isPast()); // tomorrow
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
}
