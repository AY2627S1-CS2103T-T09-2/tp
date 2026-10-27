package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Tests whether a contact matches optional status and tag filters.
 */
public class PersonMatchesFiltersPredicate implements Predicate<Person> {

    private final Optional<Status> status;
    private final Optional<Tag> tag;
    private final Optional<String> tagName;

    /**
     * Creates a predicate that requires every supplied filter to match.
     *
     * @param status Required status, or empty to accept every status.
     * @param tag Required tag, or empty to accept every tag.
     * @throws NullPointerException If either optional is null.
     */
    public PersonMatchesFiltersPredicate(Optional<Status> status, Optional<Tag> tag) {
        this.status = requireNonNull(status);
        // The tag works out its case-insensitive key once here, so each check below is a plain comparison.
        this.tag = requireNonNull(tag);
        this.tagName = tag.map(value -> value.tagName.toLowerCase(Locale.ROOT));
    }

    /**
     * Returns the optional status filter.
     */
    public Optional<Status> getStatus() {
        return status;
    }

    /**
     * Returns the optional tag filter in lowercase.
     */
    public Optional<String> getTagName() {
        return tagName;
    }

    /**
     * Returns whether the contact matches both filters, ignoring case for tags.
     *
     * @param person Contact to test.
     * @return Whether every supplied filter matches.
     * @throws NullPointerException If the contact is null.
     */
    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        boolean isStatusMatch = status.isEmpty() || person.getStatus() == status.get();
        boolean isTagMatch = tag.isEmpty() || person.getTags().stream().anyMatch(tag.get()::equals);
        return isStatusMatch && isTagMatch;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PersonMatchesFiltersPredicate otherPredicate)) {
            return false;
        }

        // Tags compare ignoring case, so t/STRASSE and t/straße give equal predicates.
        return status.equals(otherPredicate.status) && tag.equals(otherPredicate.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, tag);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("status", status)
                .add("tagName", tagName)
                .toString();
    }
}
