package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    /** Longest tag allowed, counted in Unicode code points after NFC normalization. */
    public static final int MAX_LENGTH = 50;

    /** Most combining marks, such as accents and vowel signs, that one letter may carry. */
    public static final int MAX_MARKS_PER_LETTER = 5;

    /*
     * Inputs longer than this many UTF-16 chars are rejected before NFC normalization, so very long
     * inputs cost almost nothing. NFC composes at most four code points into one, and a code point takes
     * at most two chars, so an input over 8 * MAX_LENGTH chars can never fit. This leaves extra room.
     */
    private static final int MAX_INPUT_LENGTH = 20 * MAX_LENGTH;

    public static final String MESSAGE_CONSTRAINTS =
            "Tags must be a single word of letters and digits, such as referral or lifePolicy2, up to "
                    + MAX_LENGTH + " characters long, with no spaces, symbols or invisible characters, and at most "
                    + MAX_MARKS_PER_LETTER + " accents or marks on each letter.";

    /*
     * A letter, minus the Hangul filler characters, which are classed as letters but draw nothing.
     */
    private static final String LETTER = "[\\p{L}&&[^\\u115F\\u1160\\u3164\\uFFA0]]";

    /*
     * A combining mark that can follow a letter, such as an accent or a vowel sign in Hindi or Tamil.
     * Enclosing marks and variation selectors are left out because they only change how the previous
     * character is drawn, for example turning "1" into a keycap emoji. Marks that Unicode lists as
     * default ignorable are left out because they draw nothing, so "vip" with one of them added would
     * look the same as "vip": the combining grapheme joiner, the Khmer inherent vowels and the Mongolian
     * variation selectors and vowel separator.
     */
    private static final String MARK = "[\\p{Mn}\\p{Mc}&&[^\\u034F\\u17B4\\u17B5\\u180B-\\u180F"
            + "\\uFE00-\\uFE0F\\x{E0100}-\\x{E01EF}]]";

    /*
     * A tag is one word of letters and decimal digits, such as "referral", "café" or "2026".
     * Each letter may carry up to MAX_MARKS_PER_LETTER combining marks. Real words need up to five:
     * Hindi "ज़िंदगी" has three on one letter (nukta, vowel sign and anusvara), and Burmese "လျှော့"
     * has five (two medials, a two-part vowel and a tone mark). Taller stacks are rejected, since they
     * only pile marks on top of one another. Digits are decimal digits from any script, so number symbols
     * such as "½", "²" and "①" are rejected.
     */
    public static final String VALIDATION_REGEX =
            "(?:" + LETTER + MARK + "{0," + MAX_MARKS_PER_LETTER + "}+|\\p{Nd})++";

    private static final Pattern VALIDATION_PATTERN = Pattern.compile(VALIDATION_REGEX);

    public final String tagName;

    /** Case-insensitive form of {@link #tagName}, worked out once because case mapping can be slow. */
    private final String comparisonKey;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = isAsciiLettersAndDigits(tagName) ? tagName : Normalizer.normalize(tagName, Form.NFC);
        this.comparisonKey = toComparisonKey(this.tagName);
    }

    /**
     * Returns true if a given string is a valid tag name after Unicode NFC normalization.
     * The length is checked first, so an overly long input is rejected without further work.
     */
    public static boolean isValidTagName(String test) {
        if (isAsciiLettersAndDigits(test)) {
            // Most tags are plain ASCII, which is already in NFC and always matches the pattern.
            return test.length() <= MAX_LENGTH;
        }
        if (test.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        String composed = Normalizer.normalize(test, Form.NFC);
        if (composed.codePointCount(0, composed.length()) > MAX_LENGTH) {
            return false;
        }
        return VALIDATION_PATTERN.matcher(composed).matches();
    }

    /**
     * Returns true if {@code test} is not empty and has only ASCII letters and digits.
     */
    private static boolean isAsciiLettersAndDigits(String test) {
        if (test.isEmpty()) {
            return false;
        }
        for (int i = 0; i < test.length(); i++) {
            char c = test.charAt(i);
            boolean isAsciiLetterOrDigit = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
            if (!isAsciiLetterOrDigit) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the key used to compare tag names, so that names differing only in letter case are equal.
     * The name is lowercased and then uppercased, so letters with more than one lowercase form, such as
     * the Greek sigma, share one key. {@link Locale#ROOT} keeps the result the same whatever the user's
     * default locale is. Each step is followed by NFC normalization because changing case can turn an
     * NFC string into one that is not NFC.
     *
     * @param name A tag name that is already in NFC, as {@link #tagName} is.
     */
    private static String toComparisonKey(String name) {
        if (isAsciiLettersAndDigits(name)) {
            // For ASCII letters and digits, the steps below give the same result as uppercasing alone.
            return name.toUpperCase(Locale.ROOT);
        }
        assert Normalizer.isNormalized(name, Form.NFC) : "Tag name should already be in NFC";
        String lowerCase = Normalizer.normalize(name.toLowerCase(Locale.ROOT), Form.NFC);
        return Normalizer.normalize(lowerCase.toUpperCase(Locale.ROOT), Form.NFC);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return comparisonKey.equals(otherTag.comparisonKey);
    }

    @Override
    public int hashCode() {
        return comparisonKey.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
