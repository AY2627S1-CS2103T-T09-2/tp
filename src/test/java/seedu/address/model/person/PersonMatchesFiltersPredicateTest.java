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
        // EP: a null status optional, distinct from an empty optional meaning no filter.
        assertThrows(NullPointerException.class, () ->
                new PersonMatchesFiltersPredicate(null, Optional.empty()));

        // EP: a null tag optional.
        assertThrows(NullPointerException.class, () ->
                new PersonMatchesFiltersPredicate(Optional.empty(), null));
    }

    @Test
    public void test_noFilters_returnsTrue() {
        // EP: neither filter is supplied; status and presence of tags must not restrict the result.
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

        // EP: the status matches; different tags are irrelevant when no tag filter is supplied.
        assertTrue(predicate.test(clientReferral));
        assertTrue(predicate.test(clientFamily));

        // EP: the status differs; both other supported statuses are represented.
        assertFalse(predicate.test(prospectReferral));
        assertFalse(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_tagFilter_matchesIgnoringCase() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.of(new Tag("REFERRAL")));

        // EP: a complete tag matches ignoring case; the contact's status is irrelevant.
        assertTrue(predicate.test(clientReferral));
        assertTrue(predicate.test(prospectReferral));

        // EP: tags are present, but none matches.
        assertFalse(predicate.test(clientFamily));

        // EP: the contact has no tags.
        assertFalse(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_partialTag_returnsFalse() {
        // EP: only a tag prefix matches; filtering requires the entire tag.
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.of(new Tag("ref")));

        assertFalse(predicate.test(clientReferral));
    }

    @Test
    public void test_bothFilters_requiresBothMatches() {
        PersonMatchesFiltersPredicate predicate =
                new PersonMatchesFiltersPredicate(Optional.of(Status.CLIENT),
                        Optional.of(new Tag("referral")));

        // EP: both filters match.
        assertTrue(predicate.test(clientReferral));

        // EP: only the status matches.
        assertFalse(predicate.test(clientFamily));

        // EP: only the tag matches.
        assertFalse(predicate.test(prospectReferral));

        // EP: neither filter matches.
        assertFalse(predicate.test(inactiveWithoutTags));
    }

    @Test
    public void test_nullPerson_throwsNullPointerException() {
        // EP: null contact, even when there are no filters.
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

    @Test
    public void equalsAndHashCode_tagsEqualIgnoringCase_match() {
        // Sharp s uppercases to "SS", so t/STRASSE and t/straße filter on the same tag.
        PersonMatchesFiltersPredicate upperCase =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.of(new Tag("STRASSE")));
        PersonMatchesFiltersPredicate sharpS =
                new PersonMatchesFiltersPredicate(Optional.empty(), Optional.of(new Tag("stra\u00DFe")));

        assertEquals(upperCase, sharpS);
        assertEquals(sharpS, upperCase);
        assertEquals(upperCase.hashCode(), sharpS.hashCode());

        Person streetTag = new PersonBuilder().withTags("Stra\u00DFe").build();
        assertTrue(upperCase.test(streetTag));
        assertTrue(sharpS.test(streetTag));
        assertFalse(upperCase.test(clientFamily));
    }
}
