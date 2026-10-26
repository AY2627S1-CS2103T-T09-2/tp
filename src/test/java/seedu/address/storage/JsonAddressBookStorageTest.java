package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
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
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
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

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
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
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    @Test
    public void getBackupFilePath_dataFile_appendsBakSuffix() {
        assertEquals(Paths.get("data", "addressbook.json.bak"),
                JsonAddressBookStorage.getBackupFilePath(Paths.get("data", "addressbook.json")));
    }

    @Test
    public void readAddressBookWithBackup_notJsonFormat_backupCreatedWithSameContent() throws Exception {
        assertCorruptedFileBackedUp("notJsonFormatAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_invalidPerson_backupCreatedWithSameContent() throws Exception {
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
    public void readAddressBookWithBackup_existingBackup_backupReplaced() throws Exception {
        Path backupPath = testFolder.resolve("addressbook.json.bak");
        Files.writeString(backupPath, "old backup");
        assertCorruptedFileBackedUp("notJsonFormatAddressBook.json");
    }

    @Test
    public void readAddressBookWithBackup_missingFile_noBackup() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertFalse(storage.readAddressBookWithBackup().isPresent());
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_validFile_noBackup() throws Exception {
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());

        Optional<ReadOnlyAddressBook> readBack = storage.readAddressBookWithBackup();

        assertEquals(getTypicalAddressBook(), new AddressBook(readBack.get()));
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_backupFails_throwsDataBackupException() throws Exception {
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
