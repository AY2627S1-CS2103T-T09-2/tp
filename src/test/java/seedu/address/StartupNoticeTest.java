package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Email;
import seedu.address.storage.JsonAddressBookStorage;

public class StartupNoticeTest {

    private static final Path DATA_FILE = Paths.get("data", "addressbook.json");

    @TempDir
    public Path testFolder;

    @Test
    public void forUnloadableDataFile_invalidValue_namesFileBackupAndReason() {
        // EP: a contact has an invalid value; the reason is shown as given, after the file names.
        DataLoadingException error = new DataLoadingException(
                new IllegalValueException("Contact 3 is invalid. Emails must contain @."));
        // The paths are shown as in the status bar, starting from the current folder.
        String expected = "Could not load " + Paths.get(".", "data", "addressbook.json") + ". A copy was saved as "
                + Paths.get(".", "data", "addressbook.json.bak") + ".\n"
                + "To fix the file yourself, close the app now; your next change replaces it.\n"
                + "Reason: Contact 3 is invalid. Emails must contain @.";
        assertEquals(expected, StartupNotice.forUnloadableDataFile(DATA_FILE, error));
    }

    @Test
    public void forUnloadableDataFile_invalidContactInFile_reasonNamesContactPosition() throws IOException {
        // EP: the full loading path, with the second contact in the file invalid.
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "{\"persons\":["
                + "{\"name\":\"Alice Tan\",\"phone\":\"91234567\",\"email\":\"a@example.com\",\"address\":\"1 Road\"},"
                + "{\"name\":\"Bob Lim\",\"phone\":\"91234568\",\"email\":\"bob\",\"address\":\"2 Road\"}]}");
        DataLoadingException error = assertThrows(DataLoadingException.class,
                new JsonAddressBookStorage(dataFile)::readAddressBook);

        String notice = StartupNotice.forUnloadableDataFile(dataFile, error);

        assertTrue(notice.startsWith("Could not load " + dataFile + ". A copy was saved as " + dataFile + ".bak.\n"),
                notice);
        assertTrue(notice.endsWith("\nReason: Contact 2 is invalid. " + Email.MESSAGE_CONSTRAINTS), notice);
    }

    @Test
    public void getReason_invalidJson_plainReasonWithLine() throws IOException {
        // EP: Jackson parse error; its message and the quoted file text are replaced, and only the line is kept.
        assertEquals(String.format(StartupNotice.MESSAGE_NOT_VALID_JSON, " near line 2"),
                reasonFor("{\"persons\": [\n  {\"name\": }\n]}"));
        assertEquals(String.format(StartupNotice.MESSAGE_NOT_VALID_JSON, " near line 1"),
                reasonFor("{'persons':[]}"));
    }

    @Test
    public void getReason_fileEndsEarly_plainReasonWithoutLocation() throws IOException {
        // EP: the file stops inside a value; Jackson's column for this is wrong, so no location is given.
        assertEquals(StartupNotice.MESSAGE_FILE_ENDS_EARLY, reasonFor("{\"persons\":["));
        assertEquals(StartupNotice.MESSAGE_FILE_ENDS_EARLY, reasonFor("{\"persons\":[\n{\"name\":\"Al"));
    }

    @Test
    public void getReason_wrongLayout_plainReasonWithLine() throws IOException {
        // EP: valid JSON in the wrong shape; Jackson's messages name internal classes and quote the file.
        String lineOne = String.format(StartupNotice.MESSAGE_UNEXPECTED_LAYOUT, " near line 1");
        for (String json : List.of("{\"persons\":null}", "{\"persons\":\"abc\"}", "{\"persons\":{}}", "[]",
                "[[1]]", "5", "{\"persons\":[1]}", "{\"persons\":[{\"name\":\"A\",\"tags\":[{}]}]}")) {
            String reason = reasonFor(json);
            assertEquals(lineOne, reason, json);
        }
        assertEquals(String.format(StartupNotice.MESSAGE_UNEXPECTED_LAYOUT, " near line 2"),
                reasonFor("{\"persons\":[\n{\"name\":\"A B\",\"tags\":{\"a\":1}}]}"));
    }

    @Test
    public void getReason_emptyFile_saysFileIsEmpty() throws IOException {
        // EP: no JSON at all, with and without whitespace.
        assertEquals(JsonUtil.MESSAGE_EMPTY_FILE, reasonFor(""));
        assertEquals(JsonUtil.MESSAGE_EMPTY_FILE, reasonFor(" \r\n\t"));
    }

    @Test
    public void getReason_ownStructureCheck_keepsMessageWithColumn() throws IOException {
        // EP: errors found by our own checks have reliable locations and are shown as they are.
        assertEquals(String.format(JsonUtil.MESSAGE_DUPLICATE_KEY, "persons", 1, 15),
                reasonFor("{\"persons\":[],\"persons\":[]}"));
    }

    @Test
    public void getReason_jacksonErrorWithoutLocation_plainReasonWithoutLine() {
        // EP: a Jackson error with no location gives the plain reason with no line.
        assertEquals(String.format(StartupNotice.MESSAGE_UNEXPECTED_LAYOUT, ""),
                StartupNotice.getReason(new DataLoadingException(JsonMappingException.from((JsonParser) null, "x"))));
    }

    @Test
    public void getReason_noMessage_usesGenericReason() {
        // EP: the cause has no message at all.
        assertEquals(StartupNotice.MESSAGE_UNKNOWN_REASON,
                StartupNotice.getReason(new DataLoadingException(new IOException())));
    }

    private String reasonFor(String fileContent) throws IOException {
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, fileContent);
        DataLoadingException error = assertThrows(DataLoadingException.class,
                new JsonAddressBookStorage(dataFile)::readAddressBook);
        String reason = StartupNotice.getReason(error);
        assertFalse(reason.contains("seedu") || reason.contains("java.") || reason.contains("Source"), reason);
        return reason;
    }

    @Test
    public void forUnloadableDataFile_null_throwsNullPointerException() {
        DataLoadingException error = new DataLoadingException(new IOException("x"));
        assertThrows(NullPointerException.class, () -> StartupNotice.forUnloadableDataFile(null, error));
        assertThrows(NullPointerException.class, () -> StartupNotice.forUnloadableDataFile(DATA_FILE, null));
    }
}
