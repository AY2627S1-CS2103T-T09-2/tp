package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");
    private static final String ADD_TAG_TEST_COMMAND =
            "add n/Tag Test p/91234567 e/tag@example.com a/1 Road t/VIP";
    private static final String EDIT_TAG_TO_LOWER_CASE_COMMAND = "edit 1 t/vip";

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        // EP: unknown command words fail during parsing, before command execution.
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        // EP: recognized commands with an invalid displayed index fail during execution.
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand,
                Messages.getInvalidPersonDisplayedIndexMessage(9, model.getFilteredPersonList().size()));
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        // EP: a valid read-only command succeeds without needing to save contact data.
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        // EP: execution succeeds but saving fails with a general I/O error.
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        // EP: saving fails because of permissions; this requires a different error message.
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_editTagLetterCaseOnly_savesNewSpelling() throws Exception {
        // EP: only the letter case of a tag changes. Tags compare case-insensitively, but the data file
        // keeps the exact spelling, so the edit must still be saved and survive a reload.
        logic.execute(ADD_TAG_TEST_COMMAND);
        logic.execute(EDIT_TAG_TO_LOWER_CASE_COMMAND);

        assertEquals(List.of("vip"), getTagNames(model.getAddressBook().getPersonList().get(0)));
        ReadOnlyAddressBook reloaded = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"))
                .readAddressBook().orElseThrow();
        assertEquals(List.of("vip"), getTagNames(reloaded.getPersonList().get(0)));
    }

    @Test
    public void execute_commandsThatChangeNoData_doNotSave() throws Exception {
        // EP: read-only commands, and edits that set every field to its current value, leave the data
        // unchanged, so nothing is written. A tag letter case change is then saved exactly once.
        model.addPerson(buildTagTestPerson());
        int[] saveCount = {0};
        JsonAddressBookStorage countingStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        saveCount[0]++;
                        super.saveAddressBook(addressBook);
                    }
                };
        logic = new LogicManager(model, new StorageManager(countingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        for (String command : List.of("find Tag", "due", "help", "list", "edit 1 n/Tag Test", "edit 1 t/VIP")) {
            logic.execute(command);
            assertEquals(0, saveCount[0], "Unexpected save after: " + command);
        }

        logic.execute(EDIT_TAG_TO_LOWER_CASE_COMMAND);
        assertEquals(1, saveCount[0]);
    }

    @Test
    public void execute_tagLetterCaseEditSaveFails_restoresOriginalSpelling() {
        // EP: a tag letter case edit reaches storage, so a save failure reports an error and rolls back.
        model.addPerson(buildTagTestPerson());
        JsonAddressBookStorage failingStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json")) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw DUMMY_IO_EXCEPTION;
                    }
                };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));

        assertThrows(CommandException.class,
                String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()), () ->
                        logic.execute(EDIT_TAG_TO_LOWER_CASE_COMMAND));
        assertEquals(List.of("VIP"), getTagNames(model.getAddressBook().getPersonList().get(0)));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    private static Person buildTagTestPerson() {
        return new PersonBuilder().withName("Tag Test").withPhone("91234567").withEmail("tag@example.com")
                .withAddress("1 Road").withTags("VIP").build();
    }

    /**
     * Returns the exact tag spellings of {@code person}, sorted so the result does not depend on set order.
     */
    private static List<String> getTagNames(Person person) {
        return person.getTags().stream().map(tag -> tag.tagName).sorted().toList();
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
