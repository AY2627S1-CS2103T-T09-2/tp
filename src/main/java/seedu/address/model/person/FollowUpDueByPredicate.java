package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a contact has a follow-up on or before a cutoff date.
 */
public class FollowUpDueByPredicate implements Predicate<Person> {

    private final LocalDate cutoff;

    /**
     * Creates a predicate that accepts contacts with a follow-up on or before {@code cutoff}.
     */
    public FollowUpDueByPredicate(LocalDate cutoff) {
        this.cutoff = requireNonNull(cutoff);
    }

    /**
     * Returns true if the contact has a follow-up on or before the cutoff date.
     */
    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return person.getFollowUp().map(followUp -> followUp.isOnOrBefore(cutoff)).orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FollowUpDueByPredicate otherPredicate)) {
            return false;
        }

        return cutoff.equals(otherPredicate.cutoff);
    }

    @Override
    public int hashCode() {
        return cutoff.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("cutoff", cutoff).toString();
    }
}
