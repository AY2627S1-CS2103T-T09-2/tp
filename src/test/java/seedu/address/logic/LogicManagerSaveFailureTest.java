package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Exercises failed saves through the parser, commands, model and storage boundary.
 */
public class LogicManagerSaveFailureTest {
    private static final String ADD_COMMAND =
            "add n/Daniel Tan p/91234567 e/daniel@example.com a/Bedok st/CLIENT t/friend f/25-12-2099";
    private static final String EDIT_COMMAND =
            "edit 1 n/Changed Name p/98765432 e/changed@example.com a/Jurong st/CLIENT t/new f/31-12-2099";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_doneSaveFails_restoresDataAndView() throws Exception {
        assertFailedSaveAndRetry("done 1");
    }

    @Test
    public void execute_addSaveFails_restoresDataAndView() throws Exception {
        assertFailedSaveAndRetry(ADD_COMMAND);
    }

    @Test
    public void execute_editSaveFails_restoresDataAndView() throws Exception {
        assertFailedSaveAndRetry(EDIT_COMMAND);
    }

    @Test
    public void execute_deleteSaveFails_restoresDataAndView() throws Exception {
        assertFailedSaveAndRetry("delete 1");
    }

    @Test
    public void execute_clearSaveFails_restoresDataAndView() throws Exception {
        assertFailedSaveAndRetry("clear confirm");
    }

    @Test
    public void execute_addToEmptyBookSaveFails_restoresEmptyBook() throws Exception {
        ModelManager model = new ModelManager();
        FailingStorage storage = new FailingStorage(temporaryFolder.resolve("new.json"));
        Logic logic = createLogic(model, storage);
        storage.failure = new IOException("Disk full");

        assertThrows(CommandException.class, () -> logic.execute(ADD_COMMAND));

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_querySaveFails_restoresPreviousView() throws Exception {
        for (String command : List.of("list", "list st/CLIENT", "find Carl", "due")) {
            ModelManager model = new ModelManager(contacts(), new UserPrefs());
            FailingStorage storage = new FailingStorage(temporaryFolder.resolve("queries.json"));
            Logic logic = createLogic(model, storage);
            logic.execute("list t/followup");
            List<Person> previousView = List.copyOf(logic.getFilteredPersonList());
            storage.failure = new IOException("Disk full");

            assertThrows(CommandException.class, () -> logic.execute(command));

            assertEquals(contacts(), model.getAddressBook(), command);
            assertEquals(previousView, logic.getFilteredPersonList(), command);
        }
    }

    @Test
    public void execute_invalidCommand_doesNotSaveOrChangeData() {
        ModelManager model = new ModelManager(contacts(), new UserPrefs());
        FailingStorage storage = new FailingStorage(temporaryFolder.resolve("invalid.json"));
        Logic logic = createLogic(model, storage);

        assertThrows(ParseException.class, () -> logic.execute("done 0"));
        assertThrows(CommandException.class, () -> logic.execute("done 99"));
        assertThrows(CommandException.class, () -> logic.execute("done 3"));

        assertEquals(0, storage.saveAttempts);
        assertEquals(contacts(), model.getAddressBook());
    }

    @Test
    public void execute_editSaveFails_restoredFilterAndSortRemainLive() throws Exception {
        ModelManager model = new ModelManager(contacts(), new UserPrefs());
        FailingStorage storage = new FailingStorage(temporaryFolder.resolve("live.json"));
        Logic logic = createLogic(model, storage);
        logic.execute("due");
        storage.failure = new IOException("Disk full");

        assertThrows(CommandException.class, () -> logic.execute(EDIT_COMMAND));

        // Updating a previously hidden contact must re-evaluate the original predicate and date ordering.
        Person nowDue = new PersonBuilder(CARL).withFollowUp("31-12-1999").build();
        model.setPerson(CARL, nowDue);
        assertEquals(List.of(nowDue, contacts().getPersonList().get(1), contacts().getPersonList().get(0)),
                logic.getFilteredPersonList());
    }

    /**
     * Checks each exception against an unfiltered, filtered and due-sorted list, then retries the same command.
     */
    private void assertFailedSaveAndRetry(String command) throws Exception {
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("contacts.json"))) {
            for (String viewCommand : List.of("list", "list t/followup", "due")) {
                AddressBook original = contacts();
                ModelManager model = new ModelManager(original, new UserPrefs());
                FailingStorage storage = new FailingStorage(temporaryFolder.resolve("contacts.json"));
                Logic logic = createLogic(model, storage);
                logic.execute(viewCommand);
                ObservableList<Person> liveView = logic.getFilteredPersonList();
                List<Person> originalView = List.copyOf(liveView);
                byte[] originalJson = Files.readAllBytes(storage.getAddressBookFilePath());
                storage.failure = failure;

                // Repeated failure must not lose further data or change the index used by a retry.
                for (int attempt = 0; attempt < 2; attempt++) {
                    CommandException thrown = assertThrows(CommandException.class, () -> logic.execute(command));
                    String errorFormat = failure instanceof AccessDeniedException
                            ? LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT : LogicManager.FILE_OPS_ERROR_FORMAT;
                    assertEquals(String.format(errorFormat, failure.getMessage()), thrown.getMessage());
                    assertSame(failure, thrown.getCause());
                    assertNotEquals(original, storage.attemptedData, "Command must have changed data before saving");
                    assertEquals(original, model.getAddressBook(), command + " after " + viewCommand);
                    assertSame(liveView, logic.getFilteredPersonList());
                    assertEquals(originalView, liveView, command + " after " + viewCommand);
                    assertArrayEquals(originalJson, Files.readAllBytes(storage.getAddressBookFilePath()));
                }

                AddressBook expectedAfterRetry = storage.attemptedData;
                storage.failure = null;
                logic.execute(command);
                assertEquals(expectedAfterRetry, model.getAddressBook());
                assertEquals(expectedAfterRetry, storage.readAddressBook().orElseThrow());
            }
        }
    }

    private AddressBook contacts() {
        Person alice = new PersonBuilder(ALICE).withStatus("CLIENT").withTags("followup", "friend")
                .withFollowUp("02-01-2000").build();
        Person benson = new PersonBuilder(BENSON).withStatus("PROSPECT").withTags("followup", "referral")
                .withFollowUp("01-01-2000").build();
        return new AddressBookBuilder().withPerson(alice).withPerson(benson).withPerson(CARL).build();
    }

    private Logic createLogic(ModelManager model, FailingStorage storage) {
        return new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    /**
     * Records what reached storage, failing before touching the existing file when requested.
     */
    private static class FailingStorage extends JsonAddressBookStorage {
        private IOException failure;
        private AddressBook attemptedData;
        private int saveAttempts;

        FailingStorage(Path path) {
            super(path);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            saveAttempts++;
            attemptedData = new AddressBook(addressBook);
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
