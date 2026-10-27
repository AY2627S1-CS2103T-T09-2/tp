package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void equals_caseVariants_areEqualAndDeduplicated() {
        Tag lowerCase = new Tag("referral");
        Tag upperCase = new Tag("REFERRAL");

        assertTrue(lowerCase.equals(upperCase));
        assertEquals(lowerCase.hashCode(), upperCase.hashCode());
        assertEquals(1, new HashSet<>(List.of(lowerCase, upperCase)).size());
        assertFalse(lowerCase.equals(new Tag("client")));
    }

}
