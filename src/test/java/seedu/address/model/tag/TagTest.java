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
        assertFalse(Tag.isValidTagName("vip\u17B4")); // Khmer vowel inherent aq
        assertFalse(Tag.isValidTagName("vip\u17B5")); // Khmer vowel inherent aa
        assertFalse(Tag.isValidTagName("\u1780\u17B4")); // Khmer letter ka with an inherent vowel
        assertFalse(Tag.isValidTagName("\u1780\u17B5"));

        // EP: number symbols that are not decimal digits.
        assertFalse(Tag.isValidTagName("\u00BD")); // vulgar fraction one half
        assertFalse(Tag.isValidTagName("\u00B2")); // superscript two
        assertFalse(Tag.isValidTagName("\u2460")); // circled digit one
        assertFalse(Tag.isValidTagName("vip\u00B2"));
        assertFalse(Tag.isValidTagName("\u216B")); // Roman numeral twelve
    }

    @Test
    public void isValidTagName_combiningMarks_atMostFivePerLetter() {
        // Boundary value: five marks on one letter is the most allowed. "x" has no precomposed accented
        // forms, so every mark stays separate.
        assertTrue(Tag.isValidTagName("x\u0301"));
        assertTrue(Tag.isValidTagName("x\u0301\u0302\u0303"));
        assertTrue(Tag.isValidTagName("x\u0301\u0302\u0303\u0304\u0306"));
        assertTrue(Tag.isValidTagName("x\u0301\u0302\u0303\u0304\u0306y\u0301\u0302\u0303\u0304\u0306"));

        // Boundary value: six marks on one letter is one too many.
        assertFalse(Tag.isValidTagName("x\u0301\u0302\u0303\u0304\u0306\u0307"));
        assertFalse(Tag.isValidTagName("ab\u0301\u0302\u0303\u0304\u0306\u0307c"));

        // EP: an accent that NFC builds into the letter, such as the acute in "á", still counts.
        assertTrue(Tag.isValidTagName("a\u0301\u0302\u0303\u0304\u0306"));
        assertFalse(Tag.isValidTagName("a\u0301\u0302\u0303\u0304\u0306\u0307"));
        assertTrue(Tag.isValidTagName("\u00E1\u0302\u0303\u0304\u0306"));
        assertFalse(Tag.isValidTagName("\u00E1\u0302\u0303\u0304\u0306\u0307"));
        // Vietnamese "ệ" already carries two marks, a circumflex and a dot below.
        assertTrue(Tag.isValidTagName("\u1EC7\u0301\u0302\u0303"));
        assertFalse(Tag.isValidTagName("\u1EC7\u0301\u0302\u0303\u0304"));
        assertFalse(Tag.isValidTagName("\u1EC7\u0301\u0302\u0303\u0304\u0306"));

        // EP: spacing marks count towards the limit too.
        assertTrue(Tag.isValidTagName("\u1000" + "\u102C".repeat(Tag.MAX_MARKS_PER_LETTER)));
        assertFalse(Tag.isValidTagName("\u1000" + "\u102C".repeat(Tag.MAX_MARKS_PER_LETTER + 1)));

        // EP: a tall stack of marks on one letter.
        assertFalse(Tag.isValidTagName("z" + "\u0301\u0302\u0303\u0304\u0305".repeat(6)));
        assertFalse(Tag.isValidTagName("zalgo" + "\u0336\u0337\u0338\u0339\u033A\u033B"));
        assertThrows(IllegalArgumentException.class, Tag.MESSAGE_CONSTRAINTS, () ->
                new Tag("x\u0301\u0302\u0303\u0304\u0306\u0307"));
        assertTrue(Tag.MESSAGE_CONSTRAINTS.contains(Tag.MAX_MARKS_PER_LETTER + " accents or marks"));
    }

    @Test
    public void isValidTagName_realWordsWithSeveralMarks_returnsTrue() {
        // EP: words that put three or more marks on one letter. Each must be accepted.
        assertTrue(Tag.isValidTagName("ज़िंदगी")); // Hindi "life": nukta, vowel sign and anusvara on one letter
        assertTrue(Tag.isValidTagName("\u095B\u093F\u0902\u0926\u0917\u0940")); // same, precomposed ज़
        assertTrue(Tag.isValidTagName("ख़ुशियाँ")); // Hindi "happiness"
        assertTrue(Tag.isValidTagName("הַשָּׁנָה")); // Hebrew "the year", with dagesh, shin dot and vowel on one letter
        assertTrue(Tag.isValidTagName("ကြွေး")); // Burmese "debt", four marks
        assertTrue(Tag.isValidTagName("မြို့")); // Burmese "town", four marks
        assertTrue(Tag.isValidTagName("လျှော့")); // Burmese "reduce", five marks
        assertTrue(Tag.isValidTagName("မျှော်လင့်")); // Burmese "hope", five marks

        // EP: everyday words in other scripts, with vowel signs, viramas, tone marks or vowel points.
        assertTrue(Tag.isValidTagName("স্বাস্থ্য")); // Bengali "health"
        assertTrue(Tag.isValidTagName("வாழ்க்கை")); // Tamil "life"
        assertTrue(Tag.isValidTagName("బీమా")); // Telugu "insurance"
        assertTrue(Tag.isValidTagName("ಜೀವನ")); // Kannada "life"
        assertTrue(Tag.isValidTagName("ഇൻഷുറൻസ്")); // Malayalam "insurance"
        assertTrue(Tag.isValidTagName("ผู้หญิง")); // Thai "woman", vowel and tone mark on one letter
        assertTrue(Tag.isValidTagName("အာမခံ")); // Burmese "insurance"
        assertTrue(Tag.isValidTagName("ធានារ៉ាប់រង")); // Khmer "insurance"
        assertTrue(Tag.isValidTagName("ស្ត្រី")); // Khmer "woman", with subscript consonants
        assertTrue(Tag.isValidTagName("bảohiểm")); // Vietnamese "insurance"
        assertTrue(Tag.isValidTagName("ba\u0309ohie\u0302\u0309m")); // same, with separate accents
        assertTrue(Tag.isValidTagName("مُحَمَّد")); // Arabic with harakat
        assertTrue(Tag.isValidTagName("שָׁלוֹם")); // Hebrew "peace", with niqqud
    }

    @Test
    public void equals_realWordsWithSeveralMarks_consistentWithHashCode() {
        // EP: the same word typed with a precomposed letter or with separate marks is one tag.
        assertEqualTags("ज़िंदगी", "\u095B\u093F\u0902\u0926\u0917\u0940");
        assertEqualTags("bảohiểm", "ba\u0309ohie\u0302\u0309m");
        assertEqualTags("BẢOHIỂM", "ba\u0309ohie\u0302\u0309m");

        // EP: words that differ only in their marks are different tags.
        List<String> names = List.of("ज़िंदगी", "जिंदगी", "ज़िदगी", "လျှော့", "လျှော", "လျော့", "הַשָּׁנָה", "השנה",
                "x\u0301\u0302\u0303\u0304\u0306", "x\u0301\u0302\u0303\u0304", "x");
        for (String first : names) {
            for (String second : names) {
                Tag a = new Tag(first);
                Tag b = new Tag(second);
                assertEquals(first.equals(second), a.equals(b), first + " vs " + second);
                assertEquals(a.equals(b), b.equals(a), first + " vs " + second);
                if (a.equals(b)) {
                    assertEquals(a.hashCode(), b.hashCode(), first + " vs " + second);
                }
            }
        }
    }

    @Test
    public void isValidTagName_defaultIgnorableCharacters_returnsFalse() {
        // Characters that Unicode says should draw nothing, from DerivedCoreProperties.txt. Each would let
        // two tags that look the same, such as "vip" and "vip" with one of these added, both be saved.
        int[][] defaultIgnorableRanges = {
            {0x00AD, 0x00AD}, {0x034F, 0x034F}, {0x061C, 0x061C}, {0x115F, 0x1160}, {0x17B4, 0x17B5},
            {0x180B, 0x180F}, {0x200B, 0x200F}, {0x202A, 0x202E}, {0x2060, 0x206F}, {0x3164, 0x3164},
            {0xFE00, 0xFE0F}, {0xFEFF, 0xFEFF}, {0xFFA0, 0xFFA0}, {0xFFF0, 0xFFF8}, {0x1BCA0, 0x1BCA3},
            {0x1D173, 0x1D17A}, {0xE0000, 0xE0FFF}
        };
        for (int[] range : defaultIgnorableRanges) {
            for (int codePoint = range[0]; codePoint <= range[1]; codePoint++) {
                String invisible = Character.toString(codePoint);
                String label = String.format("U+%04X", codePoint);
                assertFalse(Tag.isValidTagName(invisible), label);
                assertFalse(Tag.isValidTagName("vip" + invisible), label);
                assertFalse(Tag.isValidTagName("v" + invisible + "ip"), label);
                assertFalse(Tag.isValidTagName("\u1780" + invisible), label); // after a Khmer letter
            }
        }
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
        // NFC sorts the combining marks on each letter, and sorting a long run of marks given in the wrong
        // order takes over ten seconds here, so the raw length must be checked before normalizing.
        String unsortedMarks = "a" + "\u0315".repeat(100_000) + "\u0300".repeat(100_000);
        assertTimeoutPreemptively(FAST, () -> assertFalse(Tag.isValidTagName(unsortedMarks)));
        assertTimeoutPreemptively(FAST, () -> assertThrows(IllegalArgumentException.class,
                Tag.MESSAGE_CONSTRAINTS, () -> new Tag(unsortedMarks)));

        // Lowercasing a long run of capital sigma takes over a minute, so the constructor must reject
        // the name before it works out the case-insensitive comparison key.
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
        // A capital with no precomposed form matches the precomposed small letter.
        assertEqualTags("\u03AA\u0301", "\u0390");
        // Lowercasing this capital gives a string that must be composed again to match.
        assertEqualTags("\u1FFC\u0342", "\u1FF7");
        // Dotless i and long s uppercase to ASCII I and S under Locale.ROOT case mapping, so they fold
        // together with i and s. This is on purpose: it matches Java's equalsIgnoreCase and keeps the
        // result the same whatever the user's default locale is.
        assertEqualTags("\u0131", "I");
        assertEqualTags("\u0131", "i");
        assertEqualTags("\u017F", "S");
        assertEqualTags("\u017F", "s");
        // Uppercasing dotless i with an accent gives I and a separate accent, which must be composed again.
        assertEqualTags("\u0131\u0300", "\u00CC");
        assertEqualTags("\u0131\u0300", "\u00EC");
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
