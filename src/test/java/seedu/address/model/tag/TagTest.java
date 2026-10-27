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
        // EP: null tag name, which violates the constructor's non-null contract.
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        // EP: empty tag name. Boundary value: zero characters, below the minimum length of one.
        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag(""));
        // EP: tag name with a space.
        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag("high value"));
    }

    @Test
    public void isValidTagName_null_throwsNullPointerException() {
        // EP: null tag name, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void isValidTagName_invalidTagNames_returnsFalse() {
        // EP: empty or whitespace only.
        assertFalse(Tag.isValidTagName(""));
        assertFalse(Tag.isValidTagName(" "));
        assertFalse(Tag.isValidTagName("\t"));

        // EP: more than one word.
        assertFalse(Tag.isValidTagName("high value"));
        assertFalse(Tag.isValidTagName(" referral"));
        assertFalse(Tag.isValidTagName("referral "));
        assertFalse(Tag.isValidTagName("high value")); // no-break space

        // EP: symbols and punctuation.
        assertFalse(Tag.isValidTagName("high-value"));
        assertFalse(Tag.isValidTagName("high_value"));
        assertFalse(Tag.isValidTagName("referral!"));
        assertFalse(Tag.isValidTagName("#vip"));
        assertFalse(Tag.isValidTagName("a/b"));
        assertFalse(Tag.isValidTagName("john@example.com"));
        assertFalse(Tag.isValidTagName("😀")); // emoji

        // EP: combining mark with no letter or digit before it.
        assertFalse(Tag.isValidTagName("́"));
        assertFalse(Tag.isValidTagName("́abc"));
    }

    @Test
    public void isValidTagName_validTagNames_returnsTrue() {
        // EP: ASCII letters, in any case.
        assertTrue(Tag.isValidTagName("referral"));
        assertTrue(Tag.isValidTagName("VIP"));
        assertTrue(Tag.isValidTagName("lifePolicy"));

        // EP: digits only, and letters mixed with digits.
        assertTrue(Tag.isValidTagName("2026"));
        assertTrue(Tag.isValidTagName("lifePolicy2"));

        // Boundary value: a single character.
        assertTrue(Tag.isValidTagName("a"));
        assertTrue(Tag.isValidTagName("7"));

        // EP: a long single word.
        assertTrue(Tag.isValidTagName("a".repeat(200)));

        // EP: non-ASCII letters, matching what names accept.
        assertTrue(Tag.isValidTagName("café"));
        assertTrue(Tag.isValidTagName("CAFÉ"));
        assertTrue(Tag.isValidTagName("straße"));
        assertTrue(Tag.isValidTagName("İstanbul"));
        assertTrue(Tag.isValidTagName("保险"));
        assertTrue(Tag.isValidTagName("عميل"));
        assertTrue(Tag.isValidTagName("हिंदी")); // contains combining vowel signs

        // EP: decomposed accent (e followed by a combining acute accent).
        assertTrue(Tag.isValidTagName("café"));
    }

    @Test
    public void constructor_decomposedAccent_storesComposedForm() {
        Tag decomposed = new Tag("café");
        Tag composed = new Tag("café");

        assertEquals("café", decomposed.tagName);
        assertEquals(composed, decomposed);
        assertEquals(composed.hashCode(), decomposed.hashCode());
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

    @Test
    public void equals_nonAsciiCaseVariants_areEqual() {
        assertEqualTags("café", "CAFÉ");
        assertEqualTags("Éclair", "éCLAIR");
        assertEqualTags("ΑΣΦΑΛΕΙΑ", "ασφαλεια");
        // Capital sharp s lowercases to the ordinary sharp s.
        assertEqualTags("STRAẞE", "straße");
    }

    @Test
    public void equals_differentLetters_notEqual() {
        assertFalse(new Tag("cafe").equals(new Tag("café")));
        // Sharp s is not the same tag as "ss".
        assertFalse(new Tag("straße").equals(new Tag("STRASSE")));
        // Turkish dotted and dotless i are different letters from ASCII i.
        assertFalse(new Tag("ı").equals(new Tag("I")));
        assertFalse(new Tag("ı").equals(new Tag("i")));
        assertFalse(new Tag("İ").equals(new Tag("i")));
        assertFalse(new Tag("İ").equals(new Tag("I")));
        assertFalse(new Tag("referral").equals(null));
        assertFalse(new Tag("referral").equals("referral"));
    }

    @Test
    public void equalsAndHashCode_trickyCasePairs_areConsistent() {
        List<String> names = List.of("i", "I", "İ", "ı", "ß", "SS", "ss", "ẞ",
                "é", "É", "é", "É", "σ", "Σ", "ς", "ǅ", "ǆ", "Ǆ", "K", "k", "K");
        for (String first : names) {
            for (String second : names) {
                Tag a = new Tag(first);
                Tag b = new Tag(second);
                assertEquals(a.equals(b), b.equals(a), first + " vs " + second);
                if (a.equals(b)) {
                    assertEquals(a.hashCode(), b.hashCode(), first + " vs " + second);
                }
                assertEquals(a.equals(b), a.hasSameNameIgnoringCase(second), first + " vs " + second);
            }
        }
    }

    @Test
    public void hasSameNameIgnoringCase() {
        Tag tag = new Tag("Café");
        assertTrue(tag.hasSameNameIgnoringCase("café"));
        assertTrue(tag.hasSameNameIgnoringCase("CAFÉ"));
        assertFalse(tag.hasSameNameIgnoringCase("cafe"));
        assertFalse(tag.hasSameNameIgnoringCase("caf"));
        assertThrows(NullPointerException.class, () -> tag.hasSameNameIgnoringCase(null));
    }

    @Test
    public void toString_wrapsNameInBrackets() {
        assertEquals("[café]", new Tag("café").toString());
    }

    private static void assertEqualTags(String first, String second) {
        Tag a = new Tag(first);
        Tag b = new Tag(second);
        assertTrue(a.equals(b), first + " vs " + second);
        assertTrue(b.equals(a), second + " vs " + first);
        assertEquals(a.hashCode(), b.hashCode(), first + " vs " + second);
        assertEquals(1, new HashSet<>(List.of(a, b)).size());
    }

}
