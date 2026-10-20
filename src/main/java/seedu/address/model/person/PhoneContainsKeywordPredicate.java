package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Tests that a {@code Person}'s phone number contains the given keyword. */
public class PhoneContainsKeywordPredicate implements Predicate<Person> {
    private final String keyword;

    public PhoneContainsKeywordPredicate(String keyword) {
        this.keyword = requireNonNull(keyword);
    }

    @Override
    public boolean test(Person person) {
        return person.getPhone().value.contains(keyword);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof PhoneContainsKeywordPredicate otherPredicate)) {
            return false;
        }
        return keyword.equals(otherPredicate.keyword);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keyword", keyword).toString();
    }
}
