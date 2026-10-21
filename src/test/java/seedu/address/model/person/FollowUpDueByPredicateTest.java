package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class FollowUpDueByPredicateTest {

    private static final LocalDate CUTOFF = LocalDate.of(2026, 10, 28);

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FollowUpDueByPredicate(null));
    }

    @Test
    public void test_followUpOnOrBeforeCutoff_returnsTrue() {
        FollowUpDueByPredicate predicate = new FollowUpDueByPredicate(CUTOFF);

        assertTrue(predicate.test(new PersonBuilder().withFollowUp("01-01-2020").build()));
        assertTrue(predicate.test(new PersonBuilder().withFollowUp("28-10-2026").build()));
    }

    @Test
    public void test_followUpAfterCutoffOrMissing_returnsFalse() {
        FollowUpDueByPredicate predicate = new FollowUpDueByPredicate(CUTOFF);

        assertFalse(predicate.test(new PersonBuilder().withFollowUp("29-10-2026").build()));
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void equals() {
        FollowUpDueByPredicate predicate = new FollowUpDueByPredicate(CUTOFF);

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new FollowUpDueByPredicate(LocalDate.of(2026, 10, 28))));
        assertFalse(predicate.equals(new FollowUpDueByPredicate(CUTOFF.plusDays(1))));
        assertFalse(predicate.equals(null));
    }
}
