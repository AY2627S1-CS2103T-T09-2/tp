package seedu.address.testutil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.LastContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";

    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Status status;
    private Set<Tag> tags;
    private Optional<FollowUp> followUp;
    private Optional<LastContact> lastContact;
    private List<InteractionNote> interactionNotes;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        status = Status.PROSPECT;
        tags = new HashSet<>();
        followUp = Optional.empty();
        lastContact = Optional.empty();
        interactionNotes = new ArrayList<>();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        status = personToCopy.getStatus();
        tags = new HashSet<>(personToCopy.getTags());
        followUp = personToCopy.getFollowUp();
        lastContact = personToCopy.getLastContact();
        interactionNotes = new ArrayList<>(personToCopy.getInteractionNotes());
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>}.
     */
    public PersonBuilder withTags(String... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person}.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person}.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person}.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the status of the person being built.
     */
    public PersonBuilder withStatus(String status) {
        this.status = Status.parse(status);
        return this;
    }

    /**
     * Sets the {@code FollowUp} of the {@code Person}.
     */
    public PersonBuilder withFollowUp(String followUp) {
        this.followUp = Optional.of(new FollowUp(followUp));
        return this;
    }

    /**
     * Sets the {@code LastContact} of the {@code Person}.
     */
    public PersonBuilder withLastContact(String lastContact) {
        this.lastContact = Optional.of(new LastContact(lastContact));
        return this;
    }

    /**
     * Creates and sets interaction notes using the supplied note text.
     */
    public PersonBuilder withInteractionNotes(String... notes) {
        interactionNotes = new ArrayList<>();

        for (String note : notes) {
            interactionNotes.add(new InteractionNote(note));
        }

        return this;
    }

    /**
     * Sets the interaction notes using existing note objects.
     */
    public PersonBuilder withInteractionNotes(InteractionNote... notes) {
        interactionNotes = new ArrayList<>(List.of(notes));
        return this;
    }

    /**
     * Builds the {@code Person}.
     */
    public Person build() {
        return new Person(
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
}
