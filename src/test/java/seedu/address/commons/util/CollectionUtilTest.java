package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CollectionUtilTest {
    @Test
    public void requireAllNonNullVarargs() {
        // EP: an empty argument array. Boundary: zero elements, so there is no null element.
        assertNullPointerExceptionNotThrown();

        // EP: non-empty arguments with no null elements, including an empty but non-null string.
        assertNullPointerExceptionNotThrown(new Object(), new Object());
        assertNullPointerExceptionNotThrown("test");
        assertNullPointerExceptionNotThrown("");

        // EP: arguments containing null; exercise the first position, including a one-element array.
        assertNullPointerExceptionThrown((Object) null);
        assertNullPointerExceptionThrown(null, "", new Object());
        assertNullPointerExceptionThrown(null, new Object(), new Object());

        // Same EP: null elements in the middle, including multiple nulls.
        assertNullPointerExceptionThrown(new Object(), null, null, "test");
        assertNullPointerExceptionThrown("", null, new Object());

        // Same EP: a null in the last position checks that every argument is visited.
        assertNullPointerExceptionThrown("", new Object(), null);
        assertNullPointerExceptionThrown(new Object(), new Object(), null);

        // EP: the array itself is null, rather than an array containing a null element.
        assertNullPointerExceptionThrown((Object[]) null);

        // EP: non-null nested collections; validation checks immediate arguments, not their contents.
        List<Object> containingNull = Arrays.asList((Object) null);
        assertNullPointerExceptionNotThrown(containingNull, new Object());
    }

    @Test
    public void requireAllNonNullCollection() {
        // EP: a collection containing null; exercise the first position and a one-element collection.
        assertNullPointerExceptionThrown(Arrays.asList((Object) null));
        assertNullPointerExceptionThrown(Arrays.asList(null, new Object(), ""));

        // Same EP: null elements in the middle, including multiple nulls.
        assertNullPointerExceptionThrown(Arrays.asList("spam", null, new Object()));
        assertNullPointerExceptionThrown(Arrays.asList("spam", null, "eggs", null, new Object()));

        // Same EP: a null in the last position checks that every element is visited.
        assertNullPointerExceptionThrown(Arrays.asList("spam", new Object(), null));
        assertNullPointerExceptionThrown(Arrays.asList(new Object(), null));

        // EP: the collection itself is null.
        assertNullPointerExceptionThrown((Collection<Object>) null);

        // EP: an empty collection. Boundary: zero elements.
        assertNullPointerExceptionNotThrown(Collections.emptyList());

        // EP: a non-empty collection containing only non-null elements.
        assertNullPointerExceptionNotThrown(Arrays.asList(new Object(), "ham", Integer.valueOf(1)));
        assertNullPointerExceptionNotThrown(Arrays.asList(new Object()));

        // EP: non-null nested collections; nulls inside them are outside this shallow check.
        List<Object> containingNull = Arrays.asList((Object) null);
        assertNullPointerExceptionNotThrown(Arrays.asList(containingNull, new Object()));
    }

    @Test
    public void isAnyNonNull() {
        // EP: no elements. Boundary: an empty array contains no non-null element.
        assertFalse(CollectionUtil.isAnyNonNull());

        // EP: all supplied elements are null. One element is the smallest non-empty representative.
        assertFalse(CollectionUtil.isAnyNonNull((Object) null));

        // EP: the array itself is null.
        assertFalse(CollectionUtil.isAnyNonNull((Object[]) null));

        // EP: at least one non-null element, with and without accompanying nulls.
        assertTrue(CollectionUtil.isAnyNonNull(new Object()));
        assertTrue(CollectionUtil.isAnyNonNull(new Object(), null));
    }

    /**
     * Asserts that {@code CollectionUtil#requireAllNonNull(Object...)} throws {@code NullPointerException}
     * if {@code objects} or any element of {@code objects} is null.
     */
    private void assertNullPointerExceptionThrown(Object... objects) {
        assertThrows(NullPointerException.class, () -> requireAllNonNull(objects));
    }

    /**
     * Asserts that {@code CollectionUtil#requireAllNonNull(Collection<?>)} throws {@code NullPointerException}
     * if {@code collection} or any element of {@code collection} is null.
     */
    private void assertNullPointerExceptionThrown(Collection<?> collection) {
        assertThrows(NullPointerException.class, () -> requireAllNonNull(collection));
    }

    private void assertNullPointerExceptionNotThrown(Object... objects) {
        requireAllNonNull(objects);
    }

    private void assertNullPointerExceptionNotThrown(Collection<?> collection) {
        requireAllNonNull(collection);
    }
}
