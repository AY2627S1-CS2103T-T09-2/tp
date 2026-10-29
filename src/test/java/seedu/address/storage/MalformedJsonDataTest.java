package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;

/**
 * Tests that data and preference files with repeated keys or deeply nested JSON fail to load
 * through the normal invalid-file path instead of losing data or crashing the app.
 */
public class MalformedJsonDataTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "MalformedJsonDataTest");

    private static final String PERSON_JSON = "{\"name\":\"Alice Tan\",\"phone\":\"91234567\","
            + "\"email\":\"alice@example.com\",\"address\":\"1 Test Road\",\"tags\":[\"vip\"]}";

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBookWithBackup_repeatedPersonsKey_backupCreatedWithSameContent() throws Exception {
        // EP: two contact lists in one file; loading either one alone would delete the other on the next save.
        assertFixtureBackedUp("duplicatePersonsKey.json",
                String.format(JsonUtil.MESSAGE_DUPLICATE_KEY, "persons", 10, 3));
        assertFixtureBackedUp("duplicatePersonsKeyReversed.json", "Duplicate field 'persons'");
        assertFixtureBackedUp("duplicatePersonsKeyNull.json", "Duplicate field 'persons'");
    }

    @Test
    public void readAddressBookWithBackup_repeatedKeyInPerson_backupCreatedWithSameContent() throws Exception {
        // EP: a repeated field inside one contact, including the null lists that used to crash startup.
        assertFixtureBackedUp("duplicateNameKey.json",
                String.format(JsonUtil.MESSAGE_DUPLICATE_KEY, "name", 9, 5));
        assertFixtureBackedUp("duplicateTagsKey.json", "Duplicate field 'tags'");
        assertFixtureBackedUp("duplicateTagsKeyNull.json", "Duplicate field 'tags'");
        assertFixtureBackedUp("duplicateInteractionNotesKeyNull.json", "Duplicate field 'interactionNotes'");
    }

    @Test
    public void readAddressBookWithBackup_deepNesting_backupCreatedWithSameContent() throws Exception {
        // EP: deeply nested junk before, around or inside the contact list, at depths that used to
        // overflow the stack and crash every launch.
        for (int depth : new int[] {JsonUtil.MAX_NESTING_DEPTH + 1, 5_000, 50_000}) {
            String arrays = "[".repeat(depth) + "]".repeat(depth);
            String objects = "{\"a\":".repeat(depth) + "{}" + "}".repeat(depth);
            for (String json : List.of(arrays, objects, "[".repeat(depth),
                    "{\"junk\":" + arrays + ",\"persons\":[]}",
                    "{\"junk\":" + objects + ",\"persons\":[" + PERSON_JSON + "]}",
                    "{\"persons\":[" + PERSON_JSON + "," + arrays + "]}",
                    "{\"persons\":[{\"name\":\"Alice Tan\",\"tags\":" + arrays + "}]}")) {
                assertJsonBackedUp(json, "JSON data is nested more than");
            }
        }
    }

    @Test
    public void readAddressBookWithBackup_unknownFieldWithinNestingLimit_loadsWithoutBackup() throws Exception {
        // EP: unknown fields are still ignored when they are nested no deeper than the limit.
        int junkDepth = JsonUtil.MAX_NESTING_DEPTH - 1;
        Path filePath = testFolder.resolve("addressbook.json");
        Files.writeString(filePath, "{\"junk\":" + "[".repeat(junkDepth) + "]".repeat(junkDepth)
                + ",\"persons\":[" + PERSON_JSON + "]}");

        assertEquals(1, new JsonAddressBookStorage(filePath).readAddressBookWithBackup().get()
                .getPersonList().size());
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readAddressBookWithBackup_savedFile_loadsWithoutBackup() throws Exception {
        // EP: every contact repeats the same keys, which is fine because each contact is its own object.
        Path filePath = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(getTypicalAddressBook());

        assertEquals(getTypicalAddressBook(), new AddressBook(storage.readAddressBookWithBackup().get()));
        assertFalse(Files.exists(JsonAddressBookStorage.getBackupFilePath(filePath)));
    }

    @Test
    public void readUserPrefs_repeatedKey_throwsDataLoadingException() {
        // EP: a repeated preference key is rejected, so the app falls back to the default preferences.
        for (String fileName : List.of("duplicateGuiSettingsKey.json", "duplicateWindowWidthKey.json")) {
            Path prefsFilePath = TEST_DATA_FOLDER.resolve(fileName);
            DataLoadingException e = assertThrows(DataLoadingException.class, () ->
                    new JsonUserPrefsStorage(prefsFilePath).readUserPrefs());
            assertTrue(e.getCause().getMessage().startsWith("Duplicate field"), e.getCause().getMessage());
        }
    }

    @Test
    public void readUserPrefs_deepNesting_throwsDataLoadingException() throws IOException {
        Path prefsFilePath = testFolder.resolve("preferences.json");
        Files.writeString(prefsFilePath, "{\"junk\":" + "[".repeat(50_000) + "]".repeat(50_000) + "}");

        DataLoadingException e = assertThrows(DataLoadingException.class, () ->
                new JsonUserPrefsStorage(prefsFilePath).readUserPrefs());
        assertTrue(e.getCause().getMessage().startsWith("JSON data is nested more than"),
                e.getCause().getMessage());
    }

    private void assertFixtureBackedUp(String fixtureName, String expectedMessageStart) throws IOException {
        assertBackedUp(Files.readAllBytes(TEST_DATA_FOLDER.resolve(fixtureName)), expectedMessageStart);
    }

    private void assertJsonBackedUp(String json, String expectedMessageStart) throws IOException {
        assertBackedUp(json.getBytes(StandardCharsets.UTF_8), expectedMessageStart);
    }

    /**
     * Writes {@code content} as the data file, then checks that loading fails with
     * {@code expectedMessageStart} and leaves the data file and a byte-identical backup in place.
     */
    private void assertBackedUp(byte[] content, String expectedMessageStart) throws IOException {
        Path filePath = testFolder.resolve("addressbook.json");
        Path backupPath = JsonAddressBookStorage.getBackupFilePath(filePath);
        Files.deleteIfExists(backupPath);
        Files.write(filePath, content);

        DataLoadingException e = assertThrows(DataLoadingException.class, () ->
                new JsonAddressBookStorage(filePath).readAddressBookWithBackup());
        assertTrue(e.getCause().getMessage().startsWith(expectedMessageStart), e.getCause().getMessage());
        assertArrayEquals(content, Files.readAllBytes(filePath));
        assertArrayEquals(content, Files.readAllBytes(backupPath));
    }
}
