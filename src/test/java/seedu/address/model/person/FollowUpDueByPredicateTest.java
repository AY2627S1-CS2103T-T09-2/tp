package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        // EP: a pending follow-up before the cutoff, including dates from earlier years.
        assertTrue(predicate.test(new PersonBuilder().withFollowUp("01-01-2020").build()));

        // EP: a pending follow-up exactly on the cutoff. Boundary value: last included day.
        assertTrue(predicate.test(new PersonBuilder().withFollowUp("28-10-2026").build()));
    }

    @Test
    public void test_followUpAfterCutoffOrMissing_returnsFalse() {
        FollowUpDueByPredicate predicate = new FollowUpDueByPredicate(CUTOFF);

        // EP: a pending follow-up after the cutoff. Boundary value: first excluded day.
        assertFalse(predicate.test(new PersonBuilder().withFollowUp("29-10-2026").build()));

        // EP: no pending follow-up, regardless of the cutoff date.
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

    @Test
    public void hashCode_sameCutoff_sameHashCode() {
        // EP: equal predicates built separately must hash the same, so they behave in hash-based collections.
        assertEquals(new FollowUpDueByPredicate(CUTOFF).hashCode(),
                new FollowUpDueByPredicate(LocalDate.of(2026, 10, 28)).hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = FollowUpDueByPredicate.class.getCanonicalName() + "{cutoff=2026-10-28}";
        assertEquals(expected, new FollowUpDueByPredicate(CUTOFF).toString());
    }
}
