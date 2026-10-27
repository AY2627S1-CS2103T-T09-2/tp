package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Pins the on-disk format of the data file.
 *
 * <p>The other storage tests save and load through the same adapter classes, so renaming a JSON key
 * such as {@code followUp} keeps them passing while saved data from older versions is silently
 * dropped on load. The fixture used here is written by hand in the exact format the app saves, and
 * must not be regenerated from the adapters.
 */
public class DataFileFormatTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "DataFileFormatTest");
    private static final Path CURRENT_FORMAT_FILE = TEST_DATA_FOLDER.resolve("currentFormat.json");

    /** A contact that uses every field. Matches the first contact in the fixture. */
    private static final Person CLARA = new PersonBuilder()
            .withName("Clara Tan")
            .withPhone("91234567")
            .withEmail("clara@example.com")
            .withAddress("10 Anson Road, #20-01")
            .withStatus("CLIENT")
            .withTags("family", "vip")
            .withFollowUp("25-12-2099")
            .withInteractionNotes(
                    new InteractionNote("Asked about a hospital plan",
                            LocalDateTime.of(2026, 10, 20, 14, 30, 15)),
                    new InteractionNote("Sent the hospital plan brochure",
                            LocalDateTime.of(2026, 10, 22, 9, 5, 0, 123_456_000)))
            .build();

    /**
     * A contact saved before status, follow-up dates and interaction notes existed.
     * Matches the second contact in the fixture.
     */
    private static final Person DANIEL_OLDER_FORMAT = new PersonBuilder()
            .withName("Daniel Lim")
            .withPhone("87654321")
            .withEmail("daniel@example.com")
            .withAddress("5 Jurong West Street 41")
            .withTags("friends")
            .build();

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_currentFormatFile_loadsEveryField() throws Exception {
        // EP: a data file in the current format, with every field filled in.
        ReadOnlyAddressBook readBack = new JsonAddressBookStorage(CURRENT_FORMAT_FILE).readAddressBook().get();

        assertEquals(List.of(CLARA, DANIEL_OLDER_FORMAT), readBack.getPersonList());
    }

    @Test
    public void readAddressBook_olderFormatContact_fillsDefaults() throws Exception {
        // Guards the fixture itself: the older contact must keep lacking the newer keys,
        // otherwise the backward compatibility check below proves nothing.
        JsonNode olderContact = new ObjectMapper().readTree(CURRENT_FORMAT_FILE.toFile()).get("persons").get(1);
        assertFalse(olderContact.has("status"));
        assertFalse(olderContact.has("followUp"));
        assertFalse(olderContact.has("interactionNotes"));

        // EP: a contact saved by an older version loads as a PROSPECT with no follow-up and no notes.
        Person loaded = new JsonAddressBookStorage(CURRENT_FORMAT_FILE).readAddressBook().get()
                .getPersonList().get(1);

        assertEquals(DANIEL_OLDER_FORMAT, loaded);
        assertEquals("PROSPECT", loaded.getStatus().name());
        assertTrue(loaded.getFollowUp().isEmpty());
        assertTrue(loaded.getInteractionNotes().isEmpty());
    }

    @Test
    public void saveAddressBook_contactWithEveryField_writesExpectedKeyNames() throws Exception {
        Path filePath = testFolder.resolve("savedAddressBook.json");
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(CLARA);
        new JsonAddressBookStorage(filePath).saveAddressBook(addressBook);

        JsonNode root = new ObjectMapper().readTree(filePath.toFile());
        assertTrue(root.has("persons"), "missing key: persons");
        assertEquals(1, root.get("persons").size());

        // Only checks that the required keys are present, not that they are the only keys, so that
        // adding a new field (such as a last-contacted date) does not break this test. Renaming or
        // removing an existing key still fails it.
        JsonNode person = root.get("persons").get(0);
        assertHasKeys(person, "name", "phone", "email", "address", "status", "tags", "followUp",
                "interactionNotes");

        assertEquals("Clara Tan", person.get("name").asText());
        assertEquals("91234567", person.get("phone").asText());
        assertEquals("clara@example.com", person.get("email").asText());
        assertEquals("10 Anson Road, #20-01", person.get("address").asText());
        assertEquals("CLIENT", person.get("status").asText());
        assertEquals("25-12-2099", person.get("followUp").asText());

        JsonNode tags = person.get("tags");
        assertTrue(tags.isArray(), "tags must be a JSON array");
        Set<String> tagNames = new HashSet<>();
        tags.forEach(tag -> {
            assertTrue(tag.isTextual(), "each tag must be a plain string");
            tagNames.add(tag.asText());
        });
        assertEquals(Set.of("family", "vip"), tagNames);

        JsonNode notes = person.get("interactionNotes");
        assertTrue(notes.isArray(), "interactionNotes must be a JSON array");
        assertEquals(2, notes.size());
        JsonNode firstNote = notes.get(0);
        assertHasKeys(firstNote, "text", "createdAt");
        assertEquals("Asked about a hospital plan", firstNote.get("text").asText());
        assertEquals("2026-10-20T14:30:15", firstNote.get("createdAt").asText());
    }

    private static void assertHasKeys(JsonNode node, String... keys) {
        for (String key : keys) {
            assertTrue(node.has(key), "missing key: " + key + " in " + node);
        }
    }
}
