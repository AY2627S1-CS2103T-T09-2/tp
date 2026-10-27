package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PhoneContainsKeywordPredicateTest {
    @Test
    public void test_phoneContainsKeyword_returnsTrue() {
        // EP: the keyword is a contiguous substring of the phone digits, even away from the start.
        PhoneContainsKeywordPredicate predicate = new PhoneContainsKeywordPredicate("9123");
        assertTrue(predicate.test(new PersonBuilder().withPhone("6591234567").build()));
    }

    @Test
    public void test_phoneDoesNotContainKeyword_returnsFalse() {
        // EP: the keyword does not occur in the phone digits.
        PhoneContainsKeywordPredicate predicate = new PhoneContainsKeywordPredicate("7777");
        assertFalse(predicate.test(new PersonBuilder().withPhone("6591234567").build()));
    }
}
