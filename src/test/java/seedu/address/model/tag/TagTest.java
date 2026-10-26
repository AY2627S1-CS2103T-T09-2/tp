package seedu.address.model.tag;

import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        // EP: null tag name, which violates the constructor's non-null contract.
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        // EP: empty tag name. Boundary value: zero characters, below the minimum length of one.
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // EP: null tag name, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

}
