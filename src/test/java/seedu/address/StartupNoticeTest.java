package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
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
        String expected = "Could not load " + DATA_FILE + ". A copy was saved as "
                + Paths.get("data", "addressbook.json.bak") + ".\n"
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
    public void forUnloadableDataFile_malformedJson_reasonIsOneLineWithLocation() throws IOException {
        // EP: Jackson parse error; its multi-line message and internal source description are left out.
        Path dataFile = testFolder.resolve("addressbook.json");
        Files.writeString(dataFile, "{\"persons\": [\n  {\"name\": }\n]}");
        DataLoadingException error = assertThrows(DataLoadingException.class,
                new JsonAddressBookStorage(dataFile)::readAddressBook);

        String reason = StartupNotice.getReason(error);

        assertTrue(reason.endsWith(" at line 2, column 13"), reason);
        assertFalse(reason.contains("\n"), reason);
        assertFalse(reason.contains("Source"), reason);
    }

    @Test
    public void getReason_noMessage_usesGenericReason() {
        // EP: the cause has no message at all.
        assertEquals(StartupNotice.MESSAGE_UNKNOWN_REASON,
                StartupNotice.getReason(new DataLoadingException(new IOException())));
    }

    @Test
    public void forUnloadableDataFile_null_throwsNullPointerException() {
        DataLoadingException error = new DataLoadingException(new IOException("x"));
        assertThrows(NullPointerException.class, () -> StartupNotice.forUnloadableDataFile(null, error));
        assertThrows(NullPointerException.class, () -> StartupNotice.forUnloadableDataFile(DATA_FILE, null));
    }
}
