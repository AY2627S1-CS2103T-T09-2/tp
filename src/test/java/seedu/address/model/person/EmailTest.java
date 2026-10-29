package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class EmailTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Email(null));
    }

    @Test
    public void constructor_invalidEmail_throwsIllegalArgumentException() {
        String invalidEmail = "";
        assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
    }

    @Test
    public void isValidEmail() {
        // EP: null input, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Email.isValidEmail(null));

        // EP: blank input rather than an email address.
        assertFalse(Email.isValidEmail(""));
        assertFalse(Email.isValidEmail(" "));

        // EP: missing local part.
        assertFalse(Email.isValidEmail("@example.com"));

        // EP: missing separator between local part and domain.
        assertFalse(Email.isValidEmail("peterjackexample.com"));

        // EP: missing domain.
        assertFalse(Email.isValidEmail("peterjack@"));

        // EP: domain labels that lack alphanumeric content or contain unsupported punctuation.
        assertFalse(Email.isValidEmail("peterjack@-"));
        assertFalse(Email.isValidEmail("peterjack@exam_ple.com"));

        // EP: whitespace inside or surrounding the email.
        assertFalse(Email.isValidEmail("peter jack@example.com"));
        assertFalse(Email.isValidEmail("peterjack@exam ple.com"));
        assertFalse(Email.isValidEmail(" peterjack@example.com"));
        assertFalse(Email.isValidEmail("peterjack@example.com "));

        // EP: more than one separator between local part and domain.
        assertFalse(Email.isValidEmail("peterjack@@example.com"));
        assertFalse(Email.isValidEmail("peter@jack@example.com"));

        // EP: local-part punctuation may not appear at the start or end.
        assertFalse(Email.isValidEmail("-peterjack@example.com"));
        assertFalse(Email.isValidEmail("peterjack-@example.com"));

        // Same multiple-separator EP: an extra separator embedded in the domain.
        assertFalse(Email.isValidEmail("peterjack@example@com"));

        // EP: empty domain labels or punctuation at a domain label's ends.
        assertFalse(Email.isValidEmail("peterjack@.example.com"));
        assertFalse(Email.isValidEmail("peterjack@example.com."));
        assertFalse(Email.isValidEmail("peterjack@-example.com"));
        assertFalse(Email.isValidEmail("peterjack@example.com-"));

        // EP: final domain label shorter than two characters.
        assertFalse(Email.isValidEmail("peterjack@example.c"));

        // EP: supported punctuation separating alphanumeric groups in the local part.
        assertTrue(Email.isValidEmail("PeterJack_1190@example.com"));
        assertTrue(Email.isValidEmail("PeterJack.1190@example.com"));
        assertTrue(Email.isValidEmail("PeterJack+1190@example.com"));
        assertTrue(Email.isValidEmail("PeterJack-1190@example.com"));

        // EP: a period next to another special character in the local part is not allowed.
        assertFalse(Email.isValidEmail("peter..jack@example.com"));
        assertFalse(Email.isValidEmail("peter.-jack@example.com"));
        assertFalse(Email.isValidEmail("peter_.jack@example.com"));

        // EP: consecutive supported non-period punctuation in the local part.
        assertTrue(Email.isValidEmail("sales__sg@example.com"));
        assertTrue(Email.isValidEmail("sales++sg@example.com"));
        assertTrue(Email.isValidEmail("sales--sg@example.com"));
        assertTrue(Email.isValidEmail("peter_-jack@example.com"));

        // EP: valid domain labels containing hyphens.
        assertTrue(Email.isValidEmail("eve@mail.a-b"));
        assertTrue(Email.isValidEmail("user@xn--bcher-kva.de"));
        assertTrue(Email.isValidEmail("user@example--domain.com"));

        // EP: valid alphanumeric local part and domain, including the shortest accepted form.
        assertTrue(Email.isValidEmail("a@bc"));
        assertTrue(Email.isValidEmail("test@localhost"));
        assertTrue(Email.isValidEmail("123@145"));

        // EP: valid combinations of punctuation, long components and multiple domain labels.
        assertTrue(Email.isValidEmail("a1+be.d@example1.com"));
        assertTrue(Email.isValidEmail("peter_jack@very-very-very-long-example.com"));
        assertTrue(Email.isValidEmail("if.you.dream.it_you.can.do.it@example.com"));
        assertTrue(Email.isValidEmail("e1234567@u.nus.edu"));
    }

    @Test
    public void isValidEmail_length() {
        String domain = "@example.com";

        // EP: email at the length limit.
        String atLimit = "a".repeat(Email.MAX_LENGTH - domain.length()) + domain; // Boundary value: exactly the limit.
        assertEquals(Email.MAX_LENGTH, atLimit.length());
        assertTrue(Email.isValidEmail(atLimit));

        // EP: email over the length limit.
        assertFalse(Email.isValidEmail("a" + atLimit)); // Boundary value: one character over the limit.

        // EP: very long emails with many local-part segments are rejected without overflowing the stack.
        assertFalse(Email.isValidEmail("a.".repeat(2500) + "a" + domain));
        assertFalse(Email.isValidEmail("a.".repeat(50000) + "a" + domain));
    }

    @Test
    public void constructor_tooLongEmail_throwsIllegalArgumentException() {
        String tooLongEmail = "a".repeat(Email.MAX_LENGTH) + "@example.com";
        assertThrows(IllegalArgumentException.class, Email.MESSAGE_CONSTRAINTS, () -> new Email(tooLongEmail));
    }

    @Test
    public void equals() {
        Email email = new Email("valid@email");

        // same values -> returns true
        assertTrue(email.equals(new Email("valid@email")));

        // same object -> returns true
        assertTrue(email.equals(email));

        // null -> returns false
        assertFalse(email.equals(null));

        // different types -> returns false
        assertFalse(email.equals(5.0f));

        // different values -> returns false
        assertFalse(email.equals(new Email("other.valid@email")));
    }
}
