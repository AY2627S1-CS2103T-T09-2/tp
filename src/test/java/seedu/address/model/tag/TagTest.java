package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;

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
        assertFalse(Tag.isValidTagName("high\u00A0value")); // no-break space

        // EP: symbols and punctuation.
        assertFalse(Tag.isValidTagName("high-value"));
        assertFalse(Tag.isValidTagName("high_value"));
        assertFalse(Tag.isValidTagName("referral!"));
        assertFalse(Tag.isValidTagName("#vip"));
        assertFalse(Tag.isValidTagName("a/b"));
        assertFalse(Tag.isValidTagName("john@example.com"));
        assertFalse(Tag.isValidTagName("😀")); // emoji

        // EP: combining mark with no letter or digit before it.
        assertFalse(Tag.isValidTagName("\u0301"));
        assertFalse(Tag.isValidTagName("\u0301abc"));
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
        assertTrue(Tag.isValidTagName("cafe\u0301"));
    }

    @Test
    public void constructor_decomposedAccent_storesComposedForm() {
        Tag decomposed = new Tag("cafe\u0301");
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
        // Sharp s uppercases to "SS", so these spellings are the same tag.
        assertEqualTags("STRA\u1E9EE", "stra\u00DFe");
        assertEqualTags("stra\u00DFe", "STRASSE");
        // Greek sigma has a final form, but both forms uppercase to the same letter.
        assertEqualTags("\u03A3", "\u03C2");
        assertEqualTags("\u03BF\u03C3", "\u039F\u03A3");
        assertEqualTags("\u03BF\u03C3", "\u03BF\u03C2");
        // Lowercasing this capital gives a string that must be composed again to match.
        assertEqualTags("\u03AA\u0301", "\u0390");
        // Dotless i uppercases to ASCII I, as in Java's equalsIgnoreCase.
        assertEqualTags("\u0131", "I");
        assertEqualTags("\u0131", "i");
    }

    @Test
    public void equals_differentLetters_notEqual() {
        assertFalse(new Tag("cafe").equals(new Tag("caf\u00E9")));
        assertFalse(new Tag("referral").equals(new Tag("referrals")));
        // Dotted capital I keeps its dot when lowercased, so it differs from ASCII i.
        assertFalse(new Tag("\u0130").equals(new Tag("i")));
        assertFalse(new Tag("\u0130").equals(new Tag("I")));
        assertFalse(new Tag("referral").equals(null));
        assertFalse(new Tag("referral").equals("referral"));
    }

    @Test
    public void equalsAndHashCode_trickyCasePairs_areConsistent() {
        List<String> names = List.of("i", "I", "\u0130", "\u0131", "\u00DF", "SS", "ss", "\u1E9E",
                "\u00E9", "\u00C9", "e\u0301", "E\u0301", "\u03C3", "\u03A3", "\u03C2",
                "\u01C5", "\u01C6", "\u01C4", "K", "k", "\u212A", "\u00B5", "\u039C", "\u017F",
                "\u03AA\u0301", "\u0390", "\u03CA\u0301");
        for (String first : names) {
            for (String second : names) {
                Tag a = new Tag(first);
                Tag b = new Tag(second);
                assertEquals(a.equals(b), b.equals(a), first + " vs " + second);
                if (a.equals(b)) {
                    assertEquals(a.hashCode(), b.hashCode(), first + " vs " + second);
                }
                assertEquals(a.equals(b), a.hasSameNameIgnoringCase(second), first + " vs " + second);
                assertTrue(a.hasSameNameIgnoringCase(first.toLowerCase(Locale.ROOT)), first);
                assertTrue(a.hasSameNameIgnoringCase(first.toUpperCase(Locale.ROOT)), first);
            }
        }
    }

    @Test
    public void hasSameNameIgnoringCase() {
        Tag tag = new Tag("Café");
        assertTrue(tag.hasSameNameIgnoringCase("café"));
        assertTrue(tag.hasSameNameIgnoringCase("CAFE\u0301"));
        assertFalse(tag.hasSameNameIgnoringCase("cafe"));
        assertFalse(tag.hasSameNameIgnoringCase("caf"));

        // The list filter passes an already lowercased name.
        Tag greek = new Tag("\u03AA\u0301");
        assertTrue(greek.hasSameNameIgnoringCase("\u03AA\u0301".toLowerCase(Locale.ROOT)));
        assertTrue(greek.hasSameNameIgnoringCase("\u0390"));
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
