package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TagTest {

    /** Generous limit for work that must not grow out of control, even on a slow machine. */
    private static final Duration FAST = Duration.ofSeconds(2);

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

        // EP: zero-width and other invisible format characters.
        assertFalse(Tag.isValidTagName("a\u200Bb")); // zero-width space
        assertFalse(Tag.isValidTagName("a\u200Cb")); // zero-width non-joiner
        assertFalse(Tag.isValidTagName("a\u200Db")); // zero-width joiner
        assertFalse(Tag.isValidTagName("\uFEFFvip")); // byte order mark
        assertFalse(Tag.isValidTagName("a\u00ADb")); // soft hyphen

        // EP: emoji, including ones built from a digit or letter plus marks.
        assertFalse(Tag.isValidTagName("vip\uD83D\uDE00"));
        assertFalse(Tag.isValidTagName("1\uFE0F\u20E3")); // keycap 1
        assertFalse(Tag.isValidTagName("vip\uFE0F")); // variation selector
        assertFalse(Tag.isValidTagName("a\u20DD")); // enclosing circle

        // EP: combining mark with no letter before it.
        assertFalse(Tag.isValidTagName("\u0301"));
        assertFalse(Tag.isValidTagName("\u0301abc"));
        assertFalse(Tag.isValidTagName("1\u0301"));

        // EP: Hangul fillers, which are classed as letters but draw nothing.
        assertFalse(Tag.isValidTagName("\u3164")); // Hangul filler
        assertFalse(Tag.isValidTagName("a\u3164b"));
        assertFalse(Tag.isValidTagName("\u115F")); // Hangul choseong filler
        assertFalse(Tag.isValidTagName("\u1160")); // Hangul jungseong filler
        assertFalse(Tag.isValidTagName("\uFFA0")); // halfwidth Hangul filler
        assertFalse(Tag.isValidTagName("vip\uFFA0"));

        // EP: invisible joiners and selectors that are classed as combining marks.
        assertFalse(Tag.isValidTagName("a\u034Fb")); // combining grapheme joiner
        assertFalse(Tag.isValidTagName("a\u180Bb")); // Mongolian free variation selector one
        assertFalse(Tag.isValidTagName("a\u180Cb"));
        assertFalse(Tag.isValidTagName("a\u180Db"));
        assertFalse(Tag.isValidTagName("a\u180Eb")); // Mongolian vowel separator
        assertFalse(Tag.isValidTagName("a\u180Fb")); // Mongolian free variation selector four

        // EP: number symbols that are not decimal digits.
        assertFalse(Tag.isValidTagName("\u00BD")); // vulgar fraction one half
        assertFalse(Tag.isValidTagName("\u00B2")); // superscript two
        assertFalse(Tag.isValidTagName("\u2460")); // circled digit one
        assertFalse(Tag.isValidTagName("vip\u00B2"));
        assertFalse(Tag.isValidTagName("\u216B")); // Roman numeral twelve
    }

    @Test
    public void isValidTagName_combiningMarks_atMostTwoPerLetter() {
        // Boundary value: two marks on one letter is the most allowed. "x" has no precomposed accented
        // forms, so NFC normalization keeps every mark separate.
        assertTrue(Tag.isValidTagName("x\u0301"));
        assertTrue(Tag.isValidTagName("x\u0301\u0302"));
        assertTrue(Tag.isValidTagName("x\u0301\u0302y\u0303\u0304"));

        // Boundary value: three marks on one letter is one too many.
        assertFalse(Tag.isValidTagName("x\u0301\u0302\u0303"));
        assertFalse(Tag.isValidTagName("ab\u0301\u0302\u0303c"));

        // EP: a tall stack of marks on one letter.
        assertFalse(Tag.isValidTagName("z" + "\u0301\u0302\u0303\u0304\u0305".repeat(6)));
    }

    @Test
    public void isValidTagName_asciiCharacters_agreeWithPattern() {
        // Plain ASCII tags skip the pattern for speed, so check that every ASCII character gets the same answer.
        for (char c = 0; c < 128; c++) {
            for (String test : List.of(String.valueOf(c), "a" + c, c + "1")) {
                assertEquals(test.matches(Tag.VALIDATION_REGEX), Tag.isValidTagName(test), "U+" + (int) c);
            }
        }
    }

    @Test
    public void isValidTagName_length_atMostFiftyCharacters() {
        // Boundary values: 50 characters is the longest allowed and 51 is one too many.
        assertTrue(Tag.isValidTagName("a".repeat(Tag.MAX_LENGTH)));
        assertFalse(Tag.isValidTagName("a".repeat(Tag.MAX_LENGTH + 1)));
        assertTrue(Tag.isValidTagName("\u4FDD".repeat(50)));
        assertFalse(Tag.isValidTagName("\u4FDD".repeat(51)));

        // EP: the limit counts characters after composing accents, so a decomposed accent counts once.
        assertTrue(Tag.isValidTagName("e\u0301".repeat(50)));
        assertFalse(Tag.isValidTagName("e\u0301".repeat(51)));

        // EP: the limit counts characters, not UTF-16 units, so letters outside the BMP count once.
        assertTrue(Tag.isValidTagName("\uD840\uDC00".repeat(50))); // CJK extension B
        assertFalse(Tag.isValidTagName("\uD840\uDC00".repeat(51)));

        // EP: a fully decomposed Greek letter takes four characters, but composes to one.
        String decomposedGreek = "\u03C9\u0313\u0342\u0345"; // omega with psili, perispomeni and ypogegrammeni
        assertTrue(Tag.isValidTagName(decomposedGreek.repeat(50)));
        assertFalse(Tag.isValidTagName(decomposedGreek.repeat(51)));

        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag("a".repeat(51)));
        assertTrue(Tag.MESSAGE_CONSTRAINTS.contains("50 characters"));
    }

    @Test
    public void isValidTagName_overlyLongInputs_rejectedQuickly() {
        // Case mapping a long run of Greek sigma takes minutes, so the length must be checked first.
        String sigmas = "\u03A3".repeat(100_000);
        assertTimeoutPreemptively(FAST, () -> assertFalse(Tag.isValidTagName(sigmas)));
        assertTimeoutPreemptively(FAST, () ->
                assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () -> new Tag(sigmas)));

        List<String> longTags = List.of("a".repeat(51), "\u03A3".repeat(51), "\u03C2".repeat(51),
                "\u00DF".repeat(51), "\u0130".repeat(51), "\u0390".repeat(51), "\u4FDD".repeat(51),
                "\u0939\u093F".repeat(51), "\u0661".repeat(51), "\uD840\uDC00".repeat(51));
        assertTimeoutPreemptively(FAST, () -> {
            for (int i = 0; i < 1_000; i++) {
                for (String longTag : longTags) {
                    assertFalse(Tag.isValidTagName(longTag));
                }
            }
        });
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
        assertTrue(Tag.isValidTagName("insuranceRenewal2026"));

        // EP: a tag that looks like a command prefix.
        assertTrue(Tag.isValidTagName("st"));

        // EP: non-ASCII letters, matching what names accept.
        assertTrue(Tag.isValidTagName("café"));
        assertTrue(Tag.isValidTagName("CAFÉ"));
        assertTrue(Tag.isValidTagName("straße"));
        assertTrue(Tag.isValidTagName("İstanbul"));
        assertTrue(Tag.isValidTagName("保险"));
        assertTrue(Tag.isValidTagName("عميل"));
        assertTrue(Tag.isValidTagName("हिंदी")); // contains combining vowel signs
        assertTrue(Tag.isValidTagName("காப்பீடு")); // Tamil, with vowel signs and virama

        // EP: digits from other scripts.
        assertTrue(Tag.isValidTagName("\u0661\u0662\u0663")); // Arabic-Indic
        assertTrue(Tag.isValidTagName("\u0967\u0968")); // Devanagari
        assertTrue(Tag.isValidTagName("policy\u0661\u0662")); // letters then Arabic-Indic digits
        assertTrue(Tag.isValidTagName("\uFF11\uFF12")); // fullwidth digits

        // EP: Hangul syllables, which are visible letters unlike the Hangul fillers.
        assertTrue(Tag.isValidTagName("\uBCF4\uD5D8")); // a Korean word for insurance

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
        // Dotless i and long s uppercase to ASCII I and S under Locale.ROOT case mapping, so they fold
        // together with i and s. This is on purpose: it matches Java's equalsIgnoreCase and keeps the
        // result the same whatever the user's default locale is.
        assertEqualTags("\u0131", "I");
        assertEqualTags("\u0131", "i");
        assertEqualTags("\u017F", "S");
        assertEqualTags("\u017F", "s");
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
            }
        }
    }

    @Test
    public void equals_caseAndAccentVariants_sameTag() {
        Tag tag = new Tag("Caf\u00E9");
        assertEquals(tag, new Tag("caf\u00E9"));
        assertEquals(tag, new Tag("CAFE\u0301"));
        assertNotEquals(tag, new Tag("cafe"));
        assertNotEquals(tag, new Tag("caf"));
        assertEquals(new Tag("\u03A3".repeat(50)), new Tag("\u03C3".repeat(50)));
    }

    @Test
    public void equalsAndHashCode_longestTags_comparedQuickly() {
        // The comparison key is worked out once when the tag is made, so comparing is a plain string check.
        Tag upperCase = new Tag("\u03A3".repeat(Tag.MAX_LENGTH));
        Tag lowerCase = new Tag("\u03C3".repeat(Tag.MAX_LENGTH - 1) + "\u03C2");
        assertTimeoutPreemptively(FAST, () -> {
            for (int i = 0; i < 2_000_000; i++) {
                assertTrue(upperCase.equals(lowerCase));
                assertEquals(upperCase.hashCode(), lowerCase.hashCode());
            }
        });
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
