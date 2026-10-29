package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: no digits (empty or whitespace-only input).
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary: zero characters.
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: non-numeric text, represented by single and multiple letters.
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero, immediately below the minimum accepted value of 1.
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: unsigned positive integers with leading zeros; the numeric value is still valid.
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: negative integers.
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));

        // EP: positive integers with an explicit plus sign, which this format forbids.
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: digits mixed with whitespace; this utility does not trim its input.
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: unsigned integers above the int range.
        // Boundary: first out-of-range value; long arithmetic prevents overflow in the test itself.
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString((long) Integer.MAX_VALUE + 1)));

        // EP: unsigned positive integers within the int range.
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary: minimum accepted value.
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for containsWordIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for word: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordIgnoreCase_nullWord_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase("typical sentence", null));
    }

    @Test
    public void containsWordIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter cannot be empty", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "  "));
    }

    @Test
    public void containsWordIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, "Word parameter should be a single word", ()
            -> StringUtil.containsWordIgnoreCase("typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordIgnoreCase_nullSentence_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(null, "abc"));
    }

    /*
     * Valid equivalence partitions for word:
     *   - any word
     *   - word containing symbols/numbers
     *   - word with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - matches first word in sentence
     *   - last word in sentence
     *   - middle word in sentence
     *   - matches multiple words
     *
     * Possible scenarios returning false:
     *   - query word matches part of a sentence word
     *   - sentence word matches part of the query word
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordIgnoreCase_validInputs_correctResult() {

        // EP: sentences containing no words, including the zero-character boundary.
        assertFalse(StringUtil.containsWordIgnoreCase("", "abc"));
        assertFalse(StringUtil.containsWordIgnoreCase("    ", "123"));

        // EP: only a partial overlap; full-word matching must reject shorter and longer queries.
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bb")); // Sentence word bigger than query word
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bbbb")); // Query word bigger than sentence word

        // EP: a complete word matches, regardless of case or surrounding whitespace.
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc", "Bbb")); // Middle word.
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("  AAA   bBb   ccc  ", "aaa")); // Sentence has extra spaces
        assertTrue(StringUtil.containsWordIgnoreCase("Aaa", "aaa")); // Only one word in sentence (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "  ccc  ")); // Leading/trailing spaces

        // EP: multiple matching occurrences; any one match is sufficient.
        assertTrue(StringUtil.containsWordIgnoreCase("AAA bBb ccc  bbb", "bbB"));
    }

    //---------------- Tests for containsWordStartingWithIgnoreCase --------------------------

    @Test
    public void containsWordStartingWithIgnoreCase_validInputs_correctResult() {
        // EP: a query matches the start of a word, regardless of position or letter case.
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Alice Tan", "ta"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Alice Tan", "ALI"));
        // EP: each part and the original hyphenated word can be matched by prefix.
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Anne-Marie Tan", "mar"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Anne-Marie Tan", "ANNE"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Anne-Marie Tan", "Anne-Marie"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Anne-Marie Tan", "anne-m"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("O'Connor", "con"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Dr. Lee", "lee"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Tan, Alice", "ali"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("Raj s/o Kumar", "o"));
        assertTrue(StringUtil.containsWordStartingWithIgnoreCase("O'Connor", "O'Con"));

        // EP: a query occurs only inside a word, so substring matching would be incorrect.
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("Alice Tan", "lic"));
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("O'Connor", "onn"));

        // EP: a query starts with a whole word but is longer than that word.
        assertFalse(StringUtil.containsWordStartingWithIgnoreCase("Alice Tan", "tanley"));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
