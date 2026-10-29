package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Email;
import seedu.address.testutil.TypicalPersons;

public class JsonSerializableAddressBookTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");
    private static final Path TYPICAL_PERSONS_FILE = TEST_DATA_FOLDER.resolve("typicalPersonsAddressBook.json");
    private static final Path INVALID_PERSON_FILE = TEST_DATA_FOLDER.resolve("invalidPersonAddressBook.json");
    private static final Path DUPLICATE_PERSON_FILE = TEST_DATA_FOLDER.resolve("duplicatePersonAddressBook.json");
    private static final Path NULL_PERSON_FILE = TEST_DATA_FOLDER.resolve("nullPersonAddressBook.json");

    @Test
    public void toModelType_typicalPersonsFile_success() throws Exception {
        // EP: every contact is valid and contact identities are distinct.
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(TYPICAL_PERSONS_FILE,
                JsonSerializableAddressBook.class).get();
        AddressBook addressBookFromFile = dataFromFile.toModelType();
        AddressBook typicalPersonsAddressBook = TypicalPersons.getTypicalAddressBook();
        assertEquals(addressBookFromFile, typicalPersonsAddressBook);
    }

    @Test
    public void toModelType_invalidPersonFile_throwsIllegalValueException() throws Exception {
        // EP: at least one contact has invalid field values.
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(INVALID_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, dataFromFile::toModelType);
    }

    @Test
    public void toModelType_duplicatePersons_throwsIllegalValueException() throws Exception {
        // EP: contacts are individually valid but have duplicate identities.
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(DUPLICATE_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, String.format(JsonSerializableAddressBook.MESSAGE_CONTACT_POSITION,
                2, JsonSerializableAddressBook.MESSAGE_DUPLICATE_PERSON), dataFromFile::toModelType);
    }

    @Test
    public void toModelType_nullPersonEntry_throwsIllegalValueException() throws Exception {
        // EP: the contact list contains a null entry instead of a contact object.
        JsonSerializableAddressBook dataFromFile = JsonUtil.readJsonFile(NULL_PERSON_FILE,
                JsonSerializableAddressBook.class).get();
        assertThrows(IllegalValueException.class, String.format(JsonSerializableAddressBook.MESSAGE_CONTACT_POSITION,
                1, JsonSerializableAddressBook.MESSAGE_NULL_PERSON), dataFromFile::toModelType);
    }

    @Test
    public void toModelType_invalidThirdContact_messageNamesPosition() throws Exception {
        // EP: the first two contacts are valid and the third has an invalid email.
        String json = "{\"persons\":[" + contactJson("Alice Tan", "alice@example.com") + ","
                + contactJson("Bob Lim", "bob@example.com") + "," + contactJson("Carl Ong", "carl@") + "]}";
        JsonSerializableAddressBook dataFromFile = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, String.format(JsonSerializableAddressBook.MESSAGE_CONTACT_POSITION,
                3, Email.MESSAGE_CONSTRAINTS), dataFromFile::toModelType);
    }

    private static String contactJson(String name, String email) {
        return "{\"name\":\"" + name + "\",\"phone\":\"91234567\",\"email\":\"" + email
                + "\",\"address\":\"1 Test Road\"}";
    }

}
