package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void constructor_extraSpaces_normalizesName() {
        assertEquals("Alice Pauline Tan", new Name("Alice  Pauline   Tan ").toString());
    }

    @Test
    public void constructor_decomposedUnicode_normalizesToNfc() {
        Name decomposedName = new Name("Jose\u0301 Núñez");

        assertEquals("José Núñez", decomposedName.fullName);
        assertEquals(new Name("José Núñez"), decomposedName);
    }

    @Test
    public void isValidName() {
        // EP: null input, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // EP: blank input with no starting letter.
        assertFalse(Name.isValidName("")); // Boundary value: zero characters.
        assertFalse(Name.isValidName(" ")); // spaces only

        // EP: characters outside the permitted letters, digits, spaces and name punctuation.
        assertFalse(Name.isValidName("^"));
        assertFalse(Name.isValidName("peter*"));

        // EP: otherwise permitted characters used before the required first letter.
        assertFalse(Name.isValidName("12345")); // must start with a letter
        assertFalse(Name.isValidName("-Alice"));
        assertFalse(Name.isValidName(" Alice"));
        assertFalse(Name.isValidName("Alice-"));
        assertFalse(Name.isValidName("Alice'"));
        assertFalse(Name.isValidName("Alice."));

        // EP: names starting with a letter and containing only letters, digits and spaces.
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names

        // EP: permitted punctuation inside a name, represented by each supported punctuation mark.
        assertTrue(Name.isValidName("Anne-Marie"));
        assertTrue(Name.isValidName("O'Connor"));
        assertTrue(Name.isValidName("Dr. Lee"));
        assertTrue(Name.isValidName("Tan, Alice"));
        assertTrue(Name.isValidName("Raj s/o Kumar"));
        assertTrue(Name.isValidName("Aisha d/o Rahman"));

        // EP: Unicode letters, represented in both composed and decomposed forms.
        assertTrue(Name.isValidName("José Núñez"));
        assertTrue(Name.isValidName("Jose\u0301 Núñez"));
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
