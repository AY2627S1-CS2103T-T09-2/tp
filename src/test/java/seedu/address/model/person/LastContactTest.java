package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class LastContactTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new LastContact(null));
    }

    @Test
    public void constructor_invalidDate_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new LastContact("31-02-2026"));
    }

    @Test
    public void isValidLastContact_validDate_returnsTrue() {
        assertTrue(LastContact.isValidLastContact("27-10-2026"));
    }

    @Test
    public void isValidLastContact_invalidDate_returnsFalse() {
        assertFalse(LastContact.isValidLastContact("31-02-2026"));
    }

    @Test
    public void isValidLastContact_wrongFormat_returnsFalse() {
        assertFalse(LastContact.isValidLastContact("2026-10-27"));
    }

    @Test
    public void toString_validDate_returnsFormattedDate() {
        LastContact lastContact = new LastContact("27-10-2026");

        assertEquals("27-10-2026", lastContact.toString());
    }

    @Test
    public void equals() {
        LastContact first = new LastContact("27-10-2026");
        LastContact same = new LastContact("27-10-2026");
        LastContact different = new LastContact("26-10-2026");

        assertEquals(first, same);
        assertFalse(first.equals(different));
        assertFalse(first.equals(null));
    }
}
