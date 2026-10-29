package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_NULL_PERSON = "Persons list contains a null entry.";
    /** Prefix that says which contact, counting from 1 in file order, a loading error is about. */
    public static final String MESSAGE_CONTACT_POSITION = "Contact %d is invalid. %s";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        this.persons.addAll(persons);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated. The message starts with
     *     the position of the offending contact, e.g. {@code Contact 3 is invalid. ...}.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (int i = 0; i < persons.size(); i++) {
            addressBook.addPerson(toModelPerson(persons.get(i), i + 1, addressBook));
        }
        return addressBook;
    }

    /**
     * Converts the contact at {@code position} (counting from 1) and checks it is not already in
     * {@code addressBook}.
     */
    private static Person toModelPerson(JsonAdaptedPerson jsonAdaptedPerson, int position,
            AddressBook addressBook) throws IllegalValueException {
        if (jsonAdaptedPerson == null) {
            throw new IllegalValueException(String.format(MESSAGE_CONTACT_POSITION, position, MESSAGE_NULL_PERSON));
        }
        Person person;
        try {
            person = jsonAdaptedPerson.toModelType();
        } catch (IllegalValueException | IllegalArgumentException e) {
            // A model constructor can still reject a value that passed the raw check, e.g. an address of
            // control characters is blank once trimmed. That is invalid data too, so the file gets backed up.
            throw new IllegalValueException(String.format(MESSAGE_CONTACT_POSITION, position, e.getMessage()), e);
        }
        if (addressBook.hasPerson(person)) {
            throw new IllegalValueException(String.format(MESSAGE_CONTACT_POSITION, position,
                    MESSAGE_DUPLICATE_PERSON));
        }
        return person;
    }

}
