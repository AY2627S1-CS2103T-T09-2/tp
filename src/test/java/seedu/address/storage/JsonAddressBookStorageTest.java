package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        // EP: no path is supplied, unlike a supplied path whose file does not exist.
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        // EP: the data file is absent, as on first launch.
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        // EP: the file exists but its syntax is not valid JSON.
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        // EP: valid JSON contains a contact with invalid field values.
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        // EP: valid and invalid contacts are mixed; loading must reject the whole file.
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // EP: the destination does not exist; saving creates a readable file.
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // EP: the destination already exists; saving replaces its previous contents.
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        // EP: the address book is null while the save path is supplied.
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        // EP: the save path is null while the address book is supplied.
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void saveAddressBook_partialWriteFails_preservesSavedFile() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        byte[] savedBytes = Files.readAllBytes(filePath);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            void writeAddressBook(ReadOnlyAddressBook addressBook, Path path) throws IOException {
                Files.writeString(path, "{ \"persons\" : [");
                throw new IOException("Simulated partial write");
            }
        };

        assertThrows(IOException.class, "Simulated partial write", () ->
                failingStorage.saveAddressBook(new AddressBook()));

        assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
        assertEquals(getTypicalAddressBook(), new AddressBook(failingStorage.readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_partialFirstWriteFails_noDataOrTemporaryFile() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void writeAddressBook(ReadOnlyAddressBook addressBook, Path path) throws IOException {
                Files.writeString(path, "{ \"persons\" : [");
                throw new IOException("Simulated partial write");
            }
        };

        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(filePath));
        assertFolderEmpty(testFolder);
    }

    @Test
    public void executeDone_partialWriteFails_restoresModelViewAndSavedJson() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Person aliceWithFollowUp = new PersonBuilder(ALICE).withFollowUp("25-12-2099").build();
        AddressBook original = new AddressBook();
        original.addPerson(aliceWithFollowUp);
        original.addPerson(HOON);
        new JsonAddressBookStorage(filePath).saveAddressBook(original);
        byte[] savedBytes = Files.readAllBytes(filePath);
        ModelManager model = new ModelManager(original, new UserPrefs());
        model.updateFilteredPersonList(person -> person.getFollowUp().isPresent());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(filePath) {
            @Override
            void writeAddressBook(ReadOnlyAddressBook addressBook, Path path) throws IOException {
                Files.writeString(path, "{ \"persons\" : [");
                throw new IOException("Simulated partial write");
            }
        };
        LogicManager logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(testFolder.resolve("preferences.json"))));

        assertThrows(CommandException.class, () -> logic.execute("done 1"));

        assertEquals(original, model.getAddressBook());
        assertEquals(List.of(aliceWithFollowUp), logic.getFilteredPersonList());
        assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
        assertEquals(original, new AddressBook(failingStorage.readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_replacementFails_preservesSavedFile() throws Exception {
        assertFailedReplacementPreservesFile(new IOException("Simulated replacement failure"));
    }

    @Test
    public void saveAddressBook_replacementDenied_preservesSavedFile() throws Exception {
        assertFailedReplacementPreservesFile(new AccessDeniedException("addressbook.json"));
    }

    @Test
    public void saveAddressBook_atomicMoveUnsupported_preservesSavedFile() throws Exception {
        assertFailedReplacementPreservesFile(new AtomicMoveNotSupportedException("temporary", "target",
                "Simulated file system without atomic moves"));
    }

    @Test
    public void saveAddressBook_firstReplacementFails_noDataOrTemporaryFile() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void replaceFile(Path temporaryFile, Path target) throws IOException {
                throw new IOException("Simulated replacement failure");
            }
        };

        assertThrows(IOException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));

        assertFalse(Files.exists(filePath));
        assertFolderEmpty(testFolder);
    }

    @Test
    public void saveAddressBook_writeUsesTemporarySibling_replacesOnlyAfterCompleteWrite() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        byte[] savedBytes = Files.readAllBytes(filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void writeAddressBook(ReadOnlyAddressBook addressBook, Path path) throws IOException {
                assertNotEquals(filePath, path);
                assertEquals(filePath.getParent(), path.getParent());
                super.writeAddressBook(addressBook, path);
                assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
            }

            @Override
            void deleteTemporaryFile(Path temporaryFile) {
                throw new AssertionError("A committed save must not run failure cleanup");
            }
        };

        storage.saveAddressBook(new AddressBook());

        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_writeAndCleanupFail_preservesOriginalFailure() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        byte[] savedBytes = Files.readAllBytes(filePath);
        IOException writeFailure = new IOException("Simulated write failure");
        IOException cleanupFailure = new IOException("Simulated cleanup failure");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void writeAddressBook(ReadOnlyAddressBook addressBook, Path path) throws IOException {
                throw writeFailure;
            }

            @Override
            void deleteTemporaryFile(Path temporaryFile) throws IOException {
                throw cleanupFailure;
            }
        };

        assertThrows(IOException.class, () -> {
            try {
                storage.saveAddressBook(new AddressBook());
            } catch (IOException e) {
                assertSame(writeFailure, e);
                assertArrayEquals(new Throwable[] {cleanupFailure}, e.getSuppressed());
                throw e;
            }
        });
        assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
    }

    @Test
    public void saveAddressBook_unexpectedWriteFailure_cleansUpTemporaryFile() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void writeAddressBook(ReadOnlyAddressBook addressBook, Path path) {
                throw new IllegalStateException("Simulated serialization failure");
            }
        };

        assertThrows(IllegalStateException.class, () -> storage.saveAddressBook(getTypicalAddressBook()));
        assertFolderEmpty(testFolder);
    }

    @Test
    public void saveAddressBook_missingParentDirectories_createsThem() throws Exception {
        Path filePath = testFolder.resolve("new").resolve("nested").resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(getTypicalAddressBook());

        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_relativePath_createsAndReplacesCorrectFile() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Path workingDirectory = Path.of("").toAbsolutePath();
        assumeTrue(workingDirectory.getRoot().equals(filePath.getRoot()));
        Path relativePath = workingDirectory.relativize(filePath);
        assertFalse(relativePath.isAbsolute());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(relativePath);

        storage.saveAddressBook(getTypicalAddressBook());
        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        storage.saveAddressBook(new AddressBook());

        assertEquals(new AddressBook(), new AddressBook(new JsonAddressBookStorage(filePath)
                .readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_directoryAtTarget_doesNotReplaceDirectory() throws Exception {
        Path filePath = Files.createDirectory(testFolder.resolve("addressbook.json"));

        assertThrows(IOException.class, () ->
                new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook()));

        assertTrue(Files.isDirectory(filePath));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    public void saveAddressBook_fileAtParent_preservesParentFile() throws Exception {
        Path parent = testFolder.resolve("data");
        Files.writeString(parent, "Do not overwrite");
        Path filePath = parent.resolve("addressbook.json");

        assertThrows(IOException.class, () ->
                new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook()));

        assertEquals("Do not overwrite", Files.readString(parent));
        assertOnlyDataFileRemains(parent);
    }

    @Test
    public void saveAddressBook_readOnlyTarget_preservesFileAndReportsPermissionError() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());
        assumeTrue(Files.getFileAttributeView(filePath, PosixFileAttributeView.class) != null);
        byte[] savedBytes = Files.readAllBytes(filePath);
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(filePath);
        try {
            Files.setPosixFilePermissions(filePath, Set.of(PosixFilePermission.OWNER_READ));
            assumeFalse(Files.isWritable(filePath), "Elevated users can write to read-only files");

            assertThrows(AccessDeniedException.class, () -> storage.saveAddressBook(new AddressBook()));

            assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
            assertOnlyDataFileRemains(filePath);
        } finally {
            Files.setPosixFilePermissions(filePath, originalPermissions);
        }
    }

    @Test
    public void saveAddressBook_readOnlyParent_preservesFileAndReportsPermissionError() throws Exception {
        Path folder = Files.createDirectory(testFolder.resolve("data"));
        Path filePath = folder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());
        assumeTrue(Files.getFileAttributeView(folder, PosixFileAttributeView.class) != null);
        byte[] savedBytes = Files.readAllBytes(filePath);
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(folder);
        try {
            Files.setPosixFilePermissions(folder, Set.of(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_EXECUTE));
            assumeFalse(Files.isWritable(folder), "Elevated users can write to read-only directories");

            assertThrows(AccessDeniedException.class, () -> storage.saveAddressBook(new AddressBook()));

            assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
            assertOnlyDataFileRemains(filePath);
        } finally {
            Files.setPosixFilePermissions(folder, originalPermissions);
        }
    }

    @Test
    public void saveAddressBook_existingPosixPermissions_preserved() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());
        assumeTrue(Files.getFileAttributeView(filePath, PosixFileAttributeView.class) != null);
        Set<PosixFilePermission> permissions = Set.of(PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ);
        Files.setPosixFilePermissions(filePath, permissions);

        storage.saveAddressBook(new AddressBook());

        assertEquals(permissions, Files.getPosixFilePermissions(filePath));
        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void saveAddressBook_symbolicLink_updatesTargetAndKeepsLink() throws Exception {
        Path target = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(target).saveAddressBook(getTypicalAddressBook());
        Path linkFolder = Files.createDirectory(testFolder.resolve("links"));
        Path link = linkFolder.resolve("addressbook.json");
        Files.createSymbolicLink(link, Path.of("..", "addressbook.json"));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(link);

        storage.saveAddressBook(new AddressBook());

        assertTrue(Files.isSymbolicLink(link));
        assertEquals(new AddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertEquals(new AddressBook(), new AddressBook(new JsonAddressBookStorage(target)
                .readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(link);
        try (var files = Files.list(testFolder)) {
            assertEquals(Set.of(target, linkFolder), Set.copyOf(files.toList()));
        }
    }

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void saveAddressBook_danglingSymbolicLink_failsWithoutReplacingLink() throws Exception {
        Path missingTarget = testFolder.resolve("missing.json");
        Path link = testFolder.resolve("addressbook.json");
        Files.createSymbolicLink(link, missingTarget);

        assertThrows(NoSuchFileException.class, () ->
                new JsonAddressBookStorage(link).saveAddressBook(getTypicalAddressBook()));

        assertTrue(Files.isSymbolicLink(link));
        assertFalse(Files.exists(missingTarget));
        assertOnlyDataFileRemains(link);
    }

    /**
     * Verifies that failure at the replacement step leaves both saved JSON and the directory unchanged.
     */
    private void assertFailedReplacementPreservesFile(IOException failure) throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        new JsonAddressBookStorage(filePath).saveAddressBook(getTypicalAddressBook());
        byte[] savedBytes = Files.readAllBytes(filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath) {
            @Override
            void replaceFile(Path temporaryFile, Path target) throws IOException {
                ReadOnlyAddressBook written = assertDoesNotThrow(() -> new JsonAddressBookStorage(temporaryFile)
                        .readAddressBook().orElseThrow());
                assertEquals(new AddressBook(), new AddressBook(written));
                throw failure;
            }
        };

        assertThrows(failure.getClass(), failure.getMessage(), () -> storage.saveAddressBook(new AddressBook()));

        assertArrayEquals(savedBytes, Files.readAllBytes(filePath));
        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBook().orElseThrow()));
        assertOnlyDataFileRemains(filePath);
    }

    /**
     * Verifies that a failed first save leaves no data or temporary file behind.
     */
    private void assertFolderEmpty(Path folder) throws IOException {
        try (var files = Files.list(folder)) {
            assertEquals(List.of(), files.toList());
        }
    }

    /**
     * Verifies that no temporary file was left beside the address book.
     */
    private void assertOnlyDataFileRemains(Path filePath) throws IOException {
        try (var files = Files.list(filePath.getParent())) {
            assertEquals(List.of(filePath), files.toList());
        }
    }

    @Test
    public void getBackupFilePath_dataFile_appendsBakSuffix() {
        assertEquals(Paths.get("data", "addressbook.json.bak"),
                JsonAddressBookStorage.getBackupFilePath(Paths.get("data", "addressbook.json")));
    }

    @Test
    public void readAddressBookWithBackup_notJsonFormat_backupCreatedWithSameContent() throws Exception {
        // EP: unreadable JSON syntax triggers a backup of the original bytes.
        assertCorruptedFileBackedUp("notJsonFormatAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_invalidPerson_backupCreatedWithSameContent() throws Exception {
        // EP: readable JSON with invalid contact data also requires a backup.
        assertCorruptedFileBackedUp("invalidPersonAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_jsonNull_backupCreatedWithSameContent() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.writeString(filePath, "null");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBookWithBackup);
        assertEquals("null", Files.readString(filePath));
        assertEquals("null", Files.readString(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_tooLongPhone_backupCreatedWithSameContent() throws Exception {
        // EP: a phone far over the length limit is invalid data, so it is backed up like any other.
        assertCorruptedFileBackedUp("tooLongPhoneAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_existingBackup_backupReplaced() throws Exception {
        // EP: a backup already exists; the newest unreadable file replaces it.
        Path backupPath = testFolder.resolve("addressbook.json.bak");
        Files.writeString(backupPath, "old backup");
        assertCorruptedFileBackedUp("notJsonFormatAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_missingFile_noBackup() throws Exception {
        // EP: the source file is absent, so there is nothing to back up.
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertFalse(storage.readAddressBookWithBackup().isPresent());
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_validFile_noBackup() throws Exception {
        // EP: the source loads successfully, so no recovery backup is needed.
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());

        Optional<ReadOnlyAddressBook> readBack = storage.readAddressBookWithBackup();

        assertEquals(getTypicalAddressBook(), new AddressBook(readBack.get()));
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_backupFails_throwsDataBackupException() throws Exception {
        // EP: reading fails and the backup destination is unusable; preserve the original data.
        Path filePath = testFolder.resolve("addressbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("notJsonFormatAddressBook.json"), filePath);
        byte[] original = Files.readAllBytes(filePath);
        // A non-empty folder at the backup path cannot be replaced, so the copy fails
        Path backupPath = JsonAddressBookStorage.getBackupFilePath(filePath);
        Files.createDirectories(backupPath.resolve("blocker"));

        assertThrows(DataBackupException.class, () -> new JsonAddressBookStorage(filePath)
                .readAddressBookWithBackup());
        assertTrue(Files.isDirectory(backupPath));
        assertArrayEquals(original, Files.readAllBytes(filePath));
    }

    @Test
    public void readAddressBookWithBackup_parentNotWritable_preservesOriginal() throws Exception {
        assumeTrue(Files.getFileStore(testFolder).supportsFileAttributeView("posix"));
        assumeTrue(!"root".equals(System.getProperty("user.name")));

        Path filePath = testFolder.resolve("addressbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve("notJsonFormatAddressBook.json"), filePath);
        byte[] original = Files.readAllBytes(filePath);
        Set<PosixFilePermission> originalPermissions = Files.getPosixFilePermissions(testFolder);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        try {
            Files.setPosixFilePermissions(testFolder,
                    EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_EXECUTE));
            assertThrows(DataBackupException.class, storage::readAddressBookWithBackup);
            assertArrayEquals(original, Files.readAllBytes(filePath));
            assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
        } finally {
            Files.setPosixFilePermissions(testFolder, originalPermissions);
        }
    }

    @Test
    public void backUpFile_notRegularFile_noBackup() throws Exception {
        // EP: the source path names a directory instead of a regular file.
        Path folder = testFolder.resolve("addressbook.json");
        Files.createDirectories(folder);

        assertFalse(JsonAddressBookStorage.backUpFile(folder).isPresent());
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(folder)));
    }

    /**
     * Copies {@code fileInTestDataFolder} to a temporary data file, reads it with backup,
     * and checks that the read fails and the backup matches the original byte for byte.
     */
    private void assertCorruptedFileBackedUp(String fileInTestDataFolder) throws IOException {
        Path filePath = testFolder.resolve("addressbook.json");
        Files.copy(TEST_DATA_FOLDER.resolve(fileInTestDataFolder), filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBookWithBackup);

        Path backupPath = JsonAddressBookStorage.getBackupFilePath(filePath);
        assertTrue(Files.exists(backupPath));
        assertEquals(-1L, Files.mismatch(filePath, backupPath));
    }
}
