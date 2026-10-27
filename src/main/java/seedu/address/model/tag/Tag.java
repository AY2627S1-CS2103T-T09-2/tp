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
     * accented letters in scripts without precomposed forms are still accepted.
     */
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{N}][\\p{L}\\p{M}\\p{N}]*";

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
     * Returns the key used to compare tags, so that tags differing only in letter case are equal.
     * {@link Locale#ROOT} keeps the result the same whatever the user's default locale is.
     */
    private String comparisonKey() {
        return tagName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if this tag has the same name as {@code otherTagName}, ignoring letter case.
     * Uses the same rule as {@link #equals(Object)}.
     */
    public boolean hasSameNameIgnoringCase(String otherTagName) {
        requireNonNull(otherTagName);
        return comparisonKey().equals(Normalizer.normalize(otherTagName, Form.NFC).toLowerCase(Locale.ROOT));
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

        return comparisonKey().equals(otherTag.comparisonKey());
    }

    @Override
    public int hashCode() {
        return comparisonKey().hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
