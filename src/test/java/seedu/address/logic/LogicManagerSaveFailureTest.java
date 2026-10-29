package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.collections.ObservableList;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
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
    public void execute_deleteNoteSaveFails_restoresDataAndView() throws Exception {
        // EP: deleting a note changes persisted data without changing the other contact fields.
        assertFailedSaveAndRetry("deletenote 1 1");
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
    public void execute_queryWithFailingStorage_appliesViewWithoutSaving() throws Exception {
        List<Person> persons = contacts().getPersonList();

        // EP: queries that replace the active filter, returning all, some or no contacts.
        assertQueryWithoutSaving("list", persons);
        assertQueryWithoutSaving("list st/CLIENT", List.of(persons.get(0)));
        assertQueryWithoutSaving("list t/referral", List.of(persons.get(1)));
        assertQueryWithoutSaving("find Carl", List.of(CARL));
        assertQueryWithoutSaving("find Nobody", List.of());

        // EP: a query that changes both the filter and the displayed ordering.
        assertQueryWithoutSaving("due", List.of(persons.get(1), persons.get(0)));

        // EP: viewing interaction history leaves the current filtered list unchanged.
        assertQueryWithoutSaving("history 1", List.of(persons.get(0), persons.get(1)));
    }

    @Test
    public void execute_nonMutatingCommandWithFailingStorage_returnsResultWithoutSaving() throws Exception {
        Map<String, CommandResult> expectedResults = Map.of(
                "help", new CommandResult(HelpCommand.SHOWING_HELP_MESSAGE, true, false),
                "exit", new CommandResult(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, false, true),
                "clear", new CommandResult(String.format(ClearCommand.MESSAGE_CONFIRM_CLEAR, 3, "contacts")));

        // EP: commands that return UI actions or a confirmation prompt without changing contact data.
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("contacts.json"))) {
            for (Map.Entry<String, CommandResult> entry : expectedResults.entrySet()) {
                ModelManager model = new ModelManager(contacts(), new UserPrefs());
                FailingStorage storage = new FailingStorage(temporaryFolder.resolve("read-only.json"));
                Logic logic = createLogic(model, storage);
                logic.execute("due");
                List<Person> previousView = List.copyOf(logic.getFilteredPersonList());
                storage.failure = failure;

                assertEquals(entry.getValue(), logic.execute(entry.getKey()));

                assertEquals(contacts(), model.getAddressBook());
                assertEquals(previousView, logic.getFilteredPersonList());
                assertEquals(0, storage.saveAttempts);
                assertFalse(Files.exists(storage.getAddressBookFilePath()));
            }
        }
    }

    @Test
    public void execute_unchangedEditWithFailingStorage_doesNotSave() throws Exception {
        // EP: an edit replaces a contact with equal data, so no write is needed.
        ModelManager model = new ModelManager(contacts(), new UserPrefs());
        FailingStorage storage = new FailingStorage(temporaryFolder.resolve("unchanged.json"));
        Logic logic = createLogic(model, storage);
        storage.failure = new IOException("Disk full");

        logic.execute("edit 1 p/" + ALICE.getPhone().value);

        assertEquals(contacts(), model.getAddressBook());
        assertEquals(contacts().getPersonList(), logic.getFilteredPersonList());
        assertEquals(0, storage.saveAttempts);
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_clearEmptyBookWithFailingStorage_doesNotSave() throws Exception {
        // EP: a confirmed clear with nothing to delete. Boundary value: zero contacts.
        ModelManager model = new ModelManager();
        FailingStorage storage = new FailingStorage(temporaryFolder.resolve("empty.json"));
        Logic logic = createLogic(model, storage);
        storage.failure = new AccessDeniedException("empty.json");

        assertEquals(new CommandResult(ClearCommand.MESSAGE_SUCCESS), logic.execute("clear confirm"));

        assertEquals(new AddressBook(), model.getAddressBook());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertEquals(0, storage.saveAttempts);
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void execute_noteSaveFails_restoresNotesAndAllowsRetry() throws Exception {
        // EP: adding only a note must attempt a save and roll back its text and timestamp on failure.
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("contacts.json"))) {
            AddressBook original = contacts();
            ModelManager model = new ModelManager(original, new UserPrefs());
            FailingStorage storage = new FailingStorage(temporaryFolder.resolve("notes.json"));
            storage.saveAddressBook(original);
            byte[] originalJson = Files.readAllBytes(storage.getAddressBookFilePath());
            Logic logic = createLogic(model, storage);
            logic.execute("due");
            ObservableList<Person> liveView = logic.getFilteredPersonList();
            List<Person> originalView = List.copyOf(liveView);
            storage.failure = failure;

            // EP: repeated failed attempts preserve existing note order, text and timestamps.
            for (int attempt = 0; attempt < 2; attempt++) {
                CommandException thrown =
                        assertThrows(CommandException.class, () -> logic.execute("note 1 nt/Discussed renewal"));
                assertSame(failure, thrown.getCause());
                assertEquals(attempt + 2, storage.saveAttempts);
                assertEquals(original, model.getAddressBook());
                assertSame(liveView, logic.getFilteredPersonList());
                assertEquals(originalView, liveView);
                assertArrayEquals(originalJson, Files.readAllBytes(storage.getAddressBookFilePath()));
            }

            // EP: retrying after storage recovers adds exactly one new note and persists it.
            storage.failure = null;
            logic.execute("note 1 nt/Discussed renewal");
            List<InteractionNote> originalNotes = originalView.get(0).getInteractionNotes();
            List<InteractionNote> updatedNotes = logic.getFilteredPersonList().get(0).getInteractionNotes();
            assertEquals(originalNotes.size() + 1, updatedNotes.size());
            assertEquals(originalNotes, updatedNotes.subList(0, originalNotes.size()));
            assertEquals("Discussed renewal", updatedNotes.get(originalNotes.size()).getValue());
            assertEquals(4, storage.saveAttempts);
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void execute_noteWithReadOnlyFolder_reportsDataFileAndAllowsRetry() throws Exception {
        // EP: the data file is writable, but its parent cannot create the temporary file needed for saving.
        Path folder = Files.createDirectory(temporaryFolder.resolve("data"));
        assumeTrue(Files.getFileStore(folder).supportsFileAttributeView("posix"));
        Path filePath = folder.resolve("addressbook.json");
        AddressBook original = contacts();
        ModelManager model = new ModelManager(original, new UserPrefs());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        byte[] originalJson = Files.readAllBytes(filePath);
        Logic logic = createLogic(model, storage);
        logic.execute("due");
        List<Person> originalView = List.copyOf(logic.getFilteredPersonList());
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(folder);

        try {
            Files.setPosixFilePermissions(folder, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_EXECUTE));
            assumeFalse(Files.isWritable(folder), "Elevated users can write to read-only directories");

            CommandException thrown = assertThrows(CommandException.class, () -> logic.execute("note 1 nt/z"));

            assertEquals(String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, filePath), thrown.getMessage());
            assertEquals(original, model.getAddressBook());
            assertEquals(originalView, logic.getFilteredPersonList());
            assertArrayEquals(originalJson, Files.readAllBytes(filePath));
            try (var files = Files.list(folder)) {
                assertEquals(List.of(filePath), files.toList());
            }
        } finally {
            Files.setPosixFilePermissions(folder, originalPermissions);
        }

        // EP: fixing the permission problem allows the same note command to be saved exactly once.
        logic.execute("note 1 nt/z");
        List<InteractionNote> notes = logic.getFilteredPersonList().get(0).getInteractionNotes();
        assertEquals(originalView.get(0).getInteractionNotes().size() + 1, notes.size());
        assertEquals("z", notes.get(notes.size() - 1).getValue());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void execute_deleteWithBlockedFolder_reportsProblemAndAllowsRetry() throws Exception {
        // EP: a file blocks either the immediate parent or a higher ancestor of the configured data file.
        for (boolean isNested : List.of(false, true)) {
            Path scenarioFolder = Files.createDirectory(temporaryFolder.resolve(isNested ? "nested" : "direct"));
            Path folder = scenarioFolder.resolve("blocked data");
            Files.writeString(folder, "Keep this file");
            Path parent = isNested ? folder.resolve("nested") : folder;
            Path filePath = parent.resolve("addressbook.json");
            AddressBook original = contacts();
            ModelManager model = new ModelManager(original, new UserPrefs());
            JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
            Logic logic = createLogic(model, storage);
            logic.execute("due");
            List<Person> originalView = List.copyOf(logic.getFilteredPersonList());

            CommandException thrown = assertThrows(CommandException.class, () -> logic.execute("delete 1"));

            assertEquals("Could not save data to file " + filePath + " because " + folder + " is not a folder.",
                    thrown.getMessage());
            assertEquals(original, model.getAddressBook());
            assertEquals(originalView, logic.getFilteredPersonList());
            assertEquals("Keep this file", Files.readString(folder));
            assertFalse(Files.exists(filePath));

            // EP: moving the blocking file aside allows a retry to create the folders and persist the deletion.
            Path movedFile = scenarioFolder.resolve("blocked-data.saved");
            Files.move(folder, movedFile);
            logic.execute("delete 1");
            assertEquals(List.of(originalView.get(1)), logic.getFilteredPersonList());
            assertEquals(original.getPersonList().size() - 1, model.getAddressBook().getPersonList().size());
            assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
            assertEquals("Keep this file", Files.readString(movedFile));
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
        // EP: general I/O failures and permission failures have distinct user-facing errors.
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("contacts.json"))) {
            // EP: unfiltered, filtered and date-sorted views must all be restored after a failed mutation.
            for (String viewCommand : List.of("list", "list t/followup", "due")) {
                AddressBook original = contacts();
                ModelManager model = new ModelManager(original, new UserPrefs());
                FailingStorage storage = new FailingStorage(temporaryFolder.resolve("contacts.json"));
                storage.saveAddressBook(original);
                Logic logic = createLogic(model, storage);
                logic.execute(viewCommand);
                ObservableList<Person> liveView = logic.getFilteredPersonList();
                List<Person> originalView = List.copyOf(liveView);
                byte[] originalJson = Files.readAllBytes(storage.getAddressBookFilePath());
                storage.failure = failure;

                // Repeated failure must not lose further data or change the index used by a retry.
                for (int attempt = 0; attempt < 2; attempt++) {
                    CommandException thrown = assertThrows(CommandException.class, () -> logic.execute(command));
                    String expectedMessage = failure instanceof AccessDeniedException
                            ? String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT,
                                    storage.getAddressBookFilePath())
                            : String.format(LogicManager.FILE_OPS_ERROR_FORMAT, failure.getMessage());
                    assertEquals(expectedMessage, thrown.getMessage());
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

    /**
     * Checks that a query applies its view without attempting to save, even when storage would reject a write.
     */
    private void assertQueryWithoutSaving(String command, List<Person> expectedView) throws Exception {
        // EP: both general I/O failures and permission failures are irrelevant to read-only commands.
        for (IOException failure : List.of(new IOException("Disk full"), new AccessDeniedException("queries.json"))) {
            ModelManager model = new ModelManager(contacts(), new UserPrefs());
            FailingStorage storage = new FailingStorage(temporaryFolder.resolve("queries.json"));
            Logic logic = createLogic(model, storage);
            logic.execute("list t/followup");
            storage.failure = failure;

            logic.execute(command);

            assertEquals(contacts(), model.getAddressBook(), command);
            assertEquals(expectedView, logic.getFilteredPersonList(), command);
            assertEquals(0, storage.saveAttempts, command);
            assertFalse(Files.exists(storage.getAddressBookFilePath()));
        }
    }

    private AddressBook contacts() {
        // Fixed timestamps make full-data comparisons verify note order, text and timestamps deterministically.
        InteractionNote firstNote = new InteractionNote("Discussed coverage", LocalDateTime.of(2026, 1, 1, 9, 0));
        InteractionNote secondNote = new InteractionNote("Requested a quote", LocalDateTime.of(2026, 1, 2, 10, 0));
        Person alice = new PersonBuilder(ALICE).withStatus("CLIENT").withTags("followup", "friend")
                .withFollowUp("02-01-2000").withInteractionNotes(firstNote, secondNote).build();
        Person benson = new PersonBuilder(BENSON).withStatus("PROSPECT").withTags("followup", "referral")
                .withFollowUp("01-01-2000").withInteractionNotes(firstNote, secondNote).build();
        return new AddressBookBuilder().withPerson(alice).withPerson(benson).withPerson(CARL).build();
    }

    private Logic createLogic(ModelManager model, JsonAddressBookStorage storage) {
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
