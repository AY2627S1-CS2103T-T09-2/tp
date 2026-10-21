package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests status and tag filtering predicates.
 */
public class PersonMatchesFiltersPredicateTest {

    private final Person clientReferral = new PersonBuilder()
            .withStatus("CLIENT").withTags("ReFeRRaL", "family").build();
    private final Person clientFamily = new PersonBuilder()
            .withStatus("CLIENT").withTags("family").build();
    private final Person prospectReferral = new PersonBuilder()
            .withStatus("PROSPECT").withTags("referral").build();
    private final Person inactiveWithoutTags = new PersonBuilder().withStatus("INACTIVE").build();

    @Test
    public void constructor_nullFilters_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new PersonMatchesFiltersPredicate(null, Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new PersonMatchesFiltersPredicate(Optional.empty(), null));
    }

    @Test
    public void test_noFilters_returnsTrue() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.empty());

        assertTrue(predicate.test(clientReferral));
        assertTrue(predicate.test(prospectReferral));
        assertTrue(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_statusFilter_matchesOnlyRequiredStatus() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.of(Status.CLIENT), Optional.empty());

        assertTrue(predicate.test(clientReferral));
        assertTrue(predicate.test(clientFamily));
        assertFalse(predicate.test(prospectReferral));
        assertFalse(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_tagFilter_matchesIgnoringCase() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.of(new Tag("REFERRAL")));

        assertTrue(predicate.test(clientReferral));
        assertTrue(predicate.test(prospectReferral));
        assertFalse(predicate.test(clientFamily));
        assertFalse(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_partialTag_returnsFalse() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.of(new Tag("ref")));

        assertFalse(predicate.test(clientReferral));
    }

    @Test
    public void test_bothFilters_requiresBothMatches() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.of(Status.CLIENT),
                        Optional.of(new Tag("referral")));

        assertTrue(predicate.test(clientReferral));
        assertFalse(predicate.test(clientFamily));
        assertFalse(predicate.test(prospectReferral));
        assertFalse(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.empty());

        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    @Test
    public void equalsAndHashCode_equivalentFilters_match() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.of(Status.CLIENT),
                        Optional.of(new Tag("referral")));
        PersonMatchesFiltersPredicate sameFilters =
                new PersonMatchesFiltersPredicate(Optional.of(Status.CLIENT),
                        Optional.of(new Tag("REFERRAL")));
        PersonMatchesFiltersPredicate differentStatus =
                new PersonMatchesFiltersPredicate(Optional.of(Status.PROSPECT),
                        Optional.of(new Tag("referral")));
        PersonMatchesFiltersPredicate differentTag =
                new PersonMatchesFiltersPredicate(Optional.of(Status.CLIENT),
                        Optional.of(new Tag("family")));

        assertTrue(predicate.equals(predicate));
        assertEquals(predicate, sameFilters);
        assertEquals(predicate.hashCode(), sameFilters.hashCode());
        assertFalse(predicate.equals(differentStatus));
        assertFalse(predicate.equals(differentTag));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("referral"));
    }
}
