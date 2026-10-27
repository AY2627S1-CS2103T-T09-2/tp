package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final String MESSAGE_CONSTRAINTS =
            "Tags must be a single word of letters and digits, with no spaces or symbols, "
                    + "e.g. referral or lifePolicy2.";

    /*
     * A tag is one word of Unicode letters and digits, such as "referral" or "café".
     * Combining marks are allowed after the first character, as in names, so that
     * accented letters and vowel signs in scripts such as Hindi and Tamil are accepted.
     * Enclosing marks and variation selectors are left out because they only change how
     * the previous character is drawn, for example turning "1" into a keycap emoji.
     */
    public static final String VALIDATION_REGEX =
            "[\\p{L}\\p{N}][\\p{L}\\p{N}\\p{Mn}\\p{Mc}&&[^\\uFE00-\\uFE0F\\x{E0100}-\\x{E01EF}]]*";

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = Normalizer.normalize(tagName, Form.NFC);
    }

    /**
     * Returns true if a given string is a valid tag name after Unicode NFC normalization.
     */
    public static boolean isValidTagName(String test) {
        return Normalizer.normalize(test, Form.NFC).matches(VALIDATION_REGEX);
    }

    /**
     * Returns the key used to compare tag names, so that names differing only in letter case are equal.
     * The name is lowercased and then uppercased, so letters with more than one lowercase form, such as
     * the Greek sigma, share one key. {@link Locale#ROOT} keeps the result the same whatever the user's
     * default locale is. Each step is followed by NFC normalization because changing case can turn an
     * NFC string into one that is not NFC.
     */
    private static String toComparisonKey(String name) {
        String composed = Normalizer.normalize(name, Form.NFC);
        String lowerCase = Normalizer.normalize(composed.toLowerCase(Locale.ROOT), Form.NFC);
        return Normalizer.normalize(lowerCase.toUpperCase(Locale.ROOT), Form.NFC);
    }

    /**
     * Returns true if this tag has the same name as {@code otherTagName}, ignoring letter case.
     * Uses the same rule as {@link #equals(Object)}.
     */
    public boolean hasSameNameIgnoringCase(String otherTagName) {
        requireNonNull(otherTagName);
        return toComparisonKey(tagName).equals(toComparisonKey(otherTagName));
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

        return toComparisonKey(tagName).equals(toComparisonKey(otherTag.tagName));
    }

    @Override
    public int hashCode() {
        return toComparisonKey(tagName).hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
