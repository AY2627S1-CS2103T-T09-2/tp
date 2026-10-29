package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void constructor_tooLongPhone_throwsIllegalArgumentException() {
        String tooLongPhone = "9".repeat(Phone.MAX_LENGTH + 1);
        assertThrows(IllegalArgumentException.class, Phone.MESSAGE_CONSTRAINTS, () -> new Phone(tooLongPhone));
    }

    @Test
    public void isValidPhone() {
        // EP: null input, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // EP: blank input with no digits.
        assertFalse(Phone.isValidPhone("")); // Boundary value: zero characters.
        assertFalse(Phone.isValidPhone(" ")); // spaces only

        // EP: fewer than the required three digits.
        assertFalse(Phone.isValidPhone("91")); // Boundary value: one digit below the minimum.

        // EP: alphabetic characters, whether alone or mixed with digits.
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits

        // EP: a leading plus does not make a number with fewer than three digits valid.
        assertFalse(Phone.isValidPhone("+12"));

        // EP: plus signs outside the single optional leading position.
        assertFalse(Phone.isValidPhone("++6591234567"));
        assertFalse(Phone.isValidPhone("912+34567"));

        // EP: separators outside the gaps between digits.
        assertFalse(Phone.isValidPhone("9123-"));
        assertFalse(Phone.isValidPhone(" 9123"));

        // repeated separators between digits
        assertFalse(Phone.isValidPhone("9123--4567"));
        assertFalse(Phone.isValidPhone("9123  4567"));

        // EP: digit-only numbers with at least three digits; longer values are also supported.
        assertTrue(Phone.isValidPhone("911")); // Boundary value: minimum of three digits.
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers

        // EP: valid formatting with internal spaces or dashes and an optional leading plus.
        assertTrue(Phone.isValidPhone("9312 1534"));
        assertTrue(Phone.isValidPhone("9123-4567"));
        assertTrue(Phone.isValidPhone("+65 9123-4567"));
    }

    @Test
    public void isValidPhone_length() {
        // EP: formatted phone at the length limit.
        String atLimit = "+65 " + "9".repeat(Phone.MAX_LENGTH - 4); // Boundary value: exactly the limit.
        assertEquals(Phone.MAX_LENGTH, atLimit.length());
        assertTrue(Phone.isValidPhone(atLimit));

        // EP: phone over the length limit.
        assertFalse(Phone.isValidPhone(atLimit + "9")); // Boundary value: one character over the limit.

        // EP: very long phones are rejected without overflowing the stack.
        assertFalse(Phone.isValidPhone("9".repeat(5000)));
        assertFalse(Phone.isValidPhone("9".repeat(100000)));
    }

    @Test
    public void validationRegex_veryLongPhone_doesNotOverflow() {
        // EP: the regex alone handles input far over the length limit, as a safety net for the length check.
        assertTrue("9".repeat(100000).matches(Phone.VALIDATION_REGEX));
        assertTrue(("1 ".repeat(50000) + "1").matches(Phone.VALIDATION_REGEX));
        assertFalse(("9".repeat(100000) + "-").matches(Phone.VALIDATION_REGEX));
    }

    @Test
    public void constructor_formattedPhone_preservesInput() {
        assertEquals("+65 9123-4567", new Phone("+65 9123-4567").value);
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }

    @Test
    public void constructor_repeatedSeparators_messageExplainsSingleSeparator() {
        // EP: two separators in a row; the error must explain the rule the input breaks.
        String expectedMessage = "Phone numbers must have at least 3 digits and at most " + Phone.MAX_LENGTH
                + " characters, may start with +, and may separate digits with a single space or dash, "
                + "e.g. +65 9123-4567.";
        assertThrows(IllegalArgumentException.class, expectedMessage, () -> new Phone("9123  4567"));
        assertThrows(IllegalArgumentException.class, expectedMessage, () -> new Phone("9123 - 4567"));
    }
}
