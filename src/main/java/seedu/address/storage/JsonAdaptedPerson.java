package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
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

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT =
            "Person's %s field is missing!";
    public static final String MESSAGE_NULL_TAG =
            "Person's tags list contains a null entry.";
    public static final String MESSAGE_NULL_INTERACTION_NOTE =
            "Person's interaction notes list contains a null entry.";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String status;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String followUp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String lastContact;

    private final List<JsonAdaptedInteractionNote> interactionNotes =
            new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(
            @JsonProperty("name") String name,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("status") String status,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("followUp") String followUp,
            @JsonProperty("lastContact") String lastContact,
            @JsonProperty("interactionNotes")
                    List<JsonAdaptedInteractionNote> interactionNotes) {

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.status = status;

        if (tags != null) {
            this.tags.addAll(tags);
        }

        this.followUp = followUp;
        this.lastContact = lastContact;

        if (interactionNotes != null) {
            this.interactionNotes.addAll(interactionNotes);
        }
    }

    public JsonAdaptedPerson(
            String name,
            String phone,
            String email,
            String address,
            List<JsonAdaptedTag> tags) {

        this(
                name,
                phone,
                email,
                address,
                null,
                tags,
                null,
                null,
                null);
    }

    public JsonAdaptedPerson(
            String name,
            String phone,
            String email,
            String address,
            String status,
            List<JsonAdaptedTag> tags) {

        this(
                name,
                phone,
                email,
                address,
                status,
                tags,
                null,
                null,
                null);
    }

    public JsonAdaptedPerson(
            String name,
            String phone,
            String email,
            String address,
            String status,
            List<JsonAdaptedTag> tags,
            String followUp) {

        this(
                name,
                phone,
                email,
                address,
                status,
                tags,
                followUp,
                null,
                null);
    }

    public JsonAdaptedPerson(
            String name,
            String phone,
            String email,
            String address,
            String status,
            List<JsonAdaptedTag> tags,
            String followUp,
            List<JsonAdaptedInteractionNote> interactionNotes) {

        this(
                name,
                phone,
                email,
                address,
                status,
                tags,
                followUp,
                null,
                interactionNotes);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        status = source.getStatus().name();

        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));

        followUp = source.getFollowUp()
                .map(FollowUp::toString)
                .orElse(null);

        lastContact = source.getLastContact()
                .map(LastContact::toString)
                .orElse(null);

        interactionNotes.addAll(
                source.getInteractionNotes().stream()
                        .map(JsonAdaptedInteractionNote::new)
                        .collect(Collectors.toList()));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's
     * {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints
     *     violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();

        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(MESSAGE_NULL_TAG);
            }

            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(
                    String.format(
                            MISSING_FIELD_MESSAGE_FORMAT,
                            Name.class.getSimpleName()));
        }

        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }

        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(
                    String.format(
                            MISSING_FIELD_MESSAGE_FORMAT,
                            Phone.class.getSimpleName()));
        }

        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }

        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(
                    String.format(
                            MISSING_FIELD_MESSAGE_FORMAT,
                            Email.class.getSimpleName()));
        }

        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }

        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(
                    String.format(
                            MISSING_FIELD_MESSAGE_FORMAT,
                            Address.class.getSimpleName()));
        }

        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }

        final Address modelAddress = new Address(address);

        if (status != null
                && !status.isBlank()
                && !Status.isValidStatus(status)) {
            throw new IllegalValueException(Status.MESSAGE_CONSTRAINTS);
        }

        final Status modelStatus =
                status == null || status.isBlank()
                        ? Status.DEFAULT
                        : Status.parse(status);

        final Set<Tag> modelTags = new HashSet<>(personTags);

        if (followUp != null && !followUp.isBlank()) {
            if (!FollowUp.hasValidFormat(followUp)) {
                throw new IllegalValueException(FollowUp.MESSAGE_CONSTRAINTS);
            }

            if (!FollowUp.isRealDate(followUp)) {
                throw new IllegalValueException(
                        String.format(FollowUp.MESSAGE_NOT_A_REAL_DATE, followUp));
            }
        }

        final Optional<FollowUp> modelFollowUp =
                followUp == null || followUp.isBlank()
                        ? Optional.empty()
                        : Optional.of(new FollowUp(followUp));

        if (lastContact != null
                && !lastContact.isBlank()
                && !LastContact.isValidLastContact(lastContact)) {
            throw new IllegalValueException(
                    LastContact.MESSAGE_CONSTRAINTS);
        }

        final Optional<LastContact> modelLastContact =
                lastContact == null || lastContact.isBlank()
                        ? Optional.empty()
                        : Optional.of(new LastContact(lastContact));

        final List<InteractionNote> modelInteractionNotes =
                new ArrayList<>();

        for (JsonAdaptedInteractionNote interactionNote : interactionNotes) {
            if (interactionNote == null) {
                throw new IllegalValueException(
                        MESSAGE_NULL_INTERACTION_NOTE);
            }

            modelInteractionNotes.add(
                    interactionNote.toModelType());
        }

        return new Person(
                modelName,
                modelPhone,
                modelEmail,
                modelAddress,
                modelStatus,
                modelTags,
                modelFollowUp,
                modelLastContact,
                modelInteractionNotes);
    }
}
