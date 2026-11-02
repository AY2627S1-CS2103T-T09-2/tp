package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Status status;
    private final Set<Tag> tags = new HashSet<>();
    private final Optional<FollowUp> followUp;
    private final Optional<LastContact> lastContact;
    private final List<InteractionNote> interactionNotes;

    /**
     * Constructs a person without a follow-up date, last contact date or interaction notes.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(
                name,
                phone,
                email,
                address,
                Status.DEFAULT,
                tags,
                Optional.empty(),
                Optional.empty(),
                List.of());
    }

    /**
     * Creates a person with the given status and no follow-up date,
     * last contact date or interaction notes.
     */
    public Person(Name name, Phone phone, Email email, Address address, Status status, Set<Tag> tags) {
        this(
                name,
                phone,
                email,
                address,
                status,
                tags,
                Optional.empty(),
                Optional.empty(),
                List.of());
    }

    /**
     * Creates a person with the given status and an optional follow-up date.
     */
    public Person(Name name, Phone phone, Email email, Address address, Status status, Set<Tag> tags,
            Optional<FollowUp> followUp) {
        this(
                name,
                phone,
                email,
                address,
                status,
                tags,
                followUp,
                Optional.empty(),
                List.of());
    }

    /**
     * Creates a person with the given status, optional follow-up date and interaction notes.
     */
    public Person(Name name, Phone phone, Email email, Address address, Status status, Set<Tag> tags,
            Optional<FollowUp> followUp, List<InteractionNote> interactionNotes) {
        this(
                name,
                phone,
                email,
                address,
                status,
                tags,
                followUp,
                Optional.empty(),
                interactionNotes);
    }

    /**
     * Creates a person with the given status, optional follow-up date,
     * optional last contact date and interaction notes.
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Status status, Set<Tag> tags,
            Optional<FollowUp> followUp, Optional<LastContact> lastContact,
            List<InteractionNote> interactionNotes) {
        requireAllNonNull(
                name,
                phone,
                email,
                address,
                status,
                tags,
                followUp,
                lastContact,
                interactionNotes);

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = status;
        this.tags.addAll(tags);
        this.followUp = followUp;
        this.lastContact = lastContact;
        this.interactionNotes = List.copyOf(interactionNotes);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns the follow-up date of this person,
     * or an empty {@code Optional} if there is none.
     */
    public Optional<FollowUp> getFollowUp() {
        return followUp;
    }

    /**
     * Returns the last contact date of this person,
     * or an empty {@code Optional} if there is none.
     */
    public Optional<LastContact> getLastContact() {
        return lastContact;
    }

    /**
     * Returns the interaction notes in the order they were recorded.
     */
    public List<InteractionNote> getInteractionNotes() {
        return interactionNotes;
    }

    /**
     * Returns true if both persons have the same name, ignoring case and extra spaces.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        if (otherPerson == null) {
            return false;
        }

        String ownName = getName().fullName.replaceAll(" +", " ").trim();
        String otherName = otherPerson.getName().fullName.replaceAll(" +", " ").trim();

        return ownName.equalsIgnoreCase(otherName);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && status == otherPerson.status
                && tags.equals(otherPerson.tags)
                && followUp.equals(otherPerson.followUp)
                && lastContact.equals(otherPerson.lastContact)
                && interactionNotes.equals(otherPerson.interactionNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                phone,
                email,
                address,
                status,
                tags,
                followUp,
                lastContact,
                interactionNotes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("status", status)
                .add("tags", tags)
                .add("followUp", followUp.map(FollowUp::toString).orElse("none"))
                .add("lastContact", lastContact.map(LastContact::toString).orElse("none"))
                .add("interactionNotes", interactionNotes)
                .toString();
    }
}
