package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PhoneContainsKeywordPredicateTest {
    @Test
    public void test_phoneContainsKeyword_returnsTrue() {
        PhoneContainsKeywordPredicate predicate = new PhoneContainsKeywordPredicate("9123");
        assertTrue(predicate.test(new PersonBuilder().withPhone("6591234567").build()));
    }

    @Test
    public void test_phoneDoesNotContainKeyword_returnsFalse() {
        PhoneContainsKeywordPredicate predicate = new PhoneContainsKeywordPredicate("7777");
        assertFalse(predicate.test(new PersonBuilder().withPhone("6591234567").build()));
    }
}
