package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests command sequences through the real parser, model and JSON storage.
 */
public class LogicManagerIntegrationTest {

    private static final String NORA_EDIT_FEEDBACK = "Edited contact: Nora Tan; Phone: 81000001; "
            + "Email: nora@example.com; Address: 1 North Road; Status: PROSPECT; ";

    @TempDir
    private Path temporaryFolder;

    private Path dataFile;
    private Model model;
    private Logic logic;
    private Person noFollowUp;
    private Person nextMonth;
    private Person nextWeek;
    private Person zaraOverdue;
    private Person amyOverdue;
    private Person oldestOverdue;
    private List<Person> originalPersons;

    @BeforeEach
    public void setUp() throws Exception {
        dataFile = temporaryFolder.resolve("addressBook.json");
        LocalDate today = LocalDate.now();
        // Keep dates away from midnight-sensitive cutoffs; fixed-clock unit tests cover exact date boundaries.
        noFollowUp = new PersonBuilder().withName("Nora Tan").withPhone("81000001")
                .withEmail("nora@example.com").withAddress("1 North Road").withTags("family").build();
        nextMonth = new PersonBuilder().withName("Ben Lim").withPhone("81000002")
                .withEmail("ben@example.com").withAddress("2 East Road").withStatus("CLIENT")
                .withTags("referral").withFollowUp(FollowUp.format(today.plusDays(30))).build();
        nextWeek = new PersonBuilder().withName("Cara Lee").withPhone("81000003")
                .withEmail("cara@example.com").withAddress("3 West Road").withStatus("CLIENT")
                .withTags("referral", "vip").withFollowUp(FollowUp.format(today.plusDays(3))).build();
        zaraOverdue = new PersonBuilder().withName("Zara Goh").withPhone("81000004")
                .withEmail("zara@example.com").withAddress("4 South Road").withStatus("CLIENT")
                .withTags("vip").withFollowUp(FollowUp.format(today.minusDays(3))).build();
        amyOverdue = new PersonBuilder().withName("amy Ng").withPhone("81000005")
                .withEmail("amy@example.com").withAddress("5 Upper Road").withStatus("INACTIVE")
                .withTags("referral").withFollowUp(FollowUp.format(today.minusDays(3))).build();
        oldestOverdue = new PersonBuilder().withName("Dan Ong").withPhone("81000006")
                .withEmail("dan@example.com").withAddress("6 Lower Road").withTags("family")
                .withFollowUp(FollowUp.format(today.minusDays(10))).build();
        // Insertion order deliberately differs from due-date and alphabetical order.
        originalPersons = List.of(noFollowUp, nextMonth, nextWeek, zaraOverdue, amyOverdue, oldestOverdue);
        initialize(originalPersons);
    }

    @Test
    public void execute_dueThenDoneFirst_completesFirstDisplayedContact() throws Exception {
        // EP: several due contacts, with a hidden contact and a later follow-up outside the cutoff.
        logic.execute("due 7");
        assertState(originalPersons, List.of(oldestOverdue, amyOverdue, zaraOverdue, nextWeek));

        // Boundary: the first displayed index differs from the first stored index.
        // EP: completion removes the selected contact from the due view; feedback must retain its original date.
        CommandResult result = logic.execute("done 1");
        assertEquals(new CommandResult(String.format(DoneCommand.MESSAGE_SUCCESS, oldestOverdue.getName(),
                oldestOverdue.getFollowUp().orElseThrow())), result);
        assertState(replace(originalPersons, oldestOverdue, complete(oldestOverdue)),
                List.of(amyOverdue, zaraOverdue, nextWeek));
    }

    @Test
    public void execute_repeatedDoneInDueView_reindexesRemainingContacts() throws Exception {
        // EP: equal dates use case-insensitive name order; each completion reports the newly selected contact's date.
        logic.execute("due 7");
        List<Person> remainingPersons = new ArrayList<>(List.of(oldestOverdue, amyOverdue, zaraOverdue, nextWeek));
        List<Person> expectedPersons = originalPersons;
        assertState(expectedPersons, remainingPersons);

        for (Person person : List.copyOf(remainingPersons)) {
            CommandResult result = logic.execute("done 1");
            assertEquals(new CommandResult(String.format(DoneCommand.MESSAGE_SUCCESS, person.getName(),
                    person.getFollowUp().orElseThrow())), result);
            expectedPersons = replace(expectedPersons, person, complete(person));
            remainingPersons.remove(0);
            assertState(expectedPersons, remainingPersons);
        }

        // Boundary: completing the last match leaves an empty due view, not an empty address book.
        assertFailure("done 1", CommandException.class);
        logic.execute("list");
        assertState(expectedPersons, expectedPersons);
    }

    @Test
    public void execute_doneAtLastDueIndex_completesUpcomingContactOnly() throws Exception {
        logic.execute("due 7");
        assertState(originalPersons, List.of(oldestOverdue, amyOverdue, zaraOverdue, nextWeek));

        // Boundary: the last valid displayed index selects a future follow-up.
        logic.execute("done 4");
        assertState(replace(originalPersons, nextWeek, complete(nextWeek)),
                List.of(oldestOverdue, amyOverdue, zaraOverdue));
    }

    @Test
    public void execute_editAfterListFilters_updatesDisplayedContact() throws Exception {
        // EP: status only, tag only, and both filters; first and last displayed indices are boundaries.
        assertFilteredEdit("list st/CLIENT", List.of(nextMonth, nextWeek, zaraOverdue));
        assertFilteredEdit("list t/ReFeRrAl", List.of(nextMonth, nextWeek, amyOverdue));
        assertFilteredEdit("list st/client t/referral", List.of(nextMonth, nextWeek));
    }

    @Test
    public void execute_clearFollowUp_reportsEmptyDateAndPreservesOtherFields() throws Exception {
        Person clearedPerson = new PersonBuilder(noFollowUp).withInteractionNotes("Called client").build();
        Person personWithFollowUp = new PersonBuilder(clearedPerson).withFollowUp("25-12-2099").build();
        initialize(List.of(nextMonth, personWithFollowUp));
        logic.execute("find Nora");

        // EP: clearing a pending date, then clearing an already-empty date, in a filtered view.
        for (int attempt = 0; attempt < 2; attempt++) {
            CommandResult result = logic.execute("edit 1 f/");
            assertEquals(NORA_EDIT_FEEDBACK + "Tags: [family]; Follow-up: none", result.getFeedbackToUser());
            assertState(List.of(nextMonth, clearedPerson), List.of(clearedPerson));
        }

        reload();
        assertState(List.of(nextMonth, clearedPerson), List.of(nextMonth, clearedPerson));
    }

    @Test
    public void execute_replaceThenClearTags_reportsTagsAndPreservesFollowUp() throws Exception {
        Person personWithFollowUp = new PersonBuilder(noFollowUp).withFollowUp("25-12-2099").build();
        initialize(List.of(personWithFollowUp));

        // EP: replacing tags with multiple values reports each tag separately in card order.
        CommandResult result = logic.execute("edit 1 t/b t/a");
        assertEquals(NORA_EDIT_FEEDBACK + "Tags: [a] [b]; Follow-up: 25-12-2099", result.getFeedbackToUser());
        Person editedPerson = new PersonBuilder(personWithFollowUp).withTags("a", "b").build();
        assertState(List.of(editedPerson), List.of(editedPerson));

        // EP: clearing populated tags and repeating the clear both explicitly report an empty tag set.
        Person clearedPerson = new PersonBuilder(personWithFollowUp).withTags().build();
        for (int attempt = 0; attempt < 2; attempt++) {
            result = logic.execute("edit 1 t/");
            assertEquals(NORA_EDIT_FEEDBACK + "Tags: none; Follow-up: 25-12-2099", result.getFeedbackToUser());
            assertState(List.of(clearedPerson), List.of(clearedPerson));
        }

        reload();
        assertState(List.of(clearedPerson), List.of(clearedPerson));
    }

    @Test
    public void execute_clearTagsAndFollowUp_reportsBothEmptyFields() throws Exception {
        Person clearedPerson = new PersonBuilder(noFollowUp).withTags().withInteractionNotes("Called client").build();
        Person originalPerson = new PersonBuilder(clearedPerson).withTags("family", "referral")
                .withFollowUp("25-12-2099").build();
        initialize(List.of(originalPerson));

        // EP: both optional fields are explicitly cleared in the same parsed command.
        CommandResult result = logic.execute("edit 1 f/ t/");
        assertEquals(NORA_EDIT_FEEDBACK + "Tags: none; Follow-up: none", result.getFeedbackToUser());
        assertState(List.of(clearedPerson), List.of(clearedPerson));
        reload();
        assertState(List.of(clearedPerson), List.of(clearedPerson));
    }

    @Test
    public void execute_deleteAfterListFilters_removesDisplayedContact() throws Exception {
        // EP: each filter type retains the other matches after deletion.
        assertFilteredDelete("list st/CLIENT", List.of(nextMonth, nextWeek, zaraOverdue));
        assertFilteredDelete("list t/ReFeRrAl", List.of(nextMonth, nextWeek, amyOverdue));
        assertFilteredDelete("list st/client t/referral", List.of(nextMonth, nextWeek));
    }

    @Test
    public void execute_doneAfterListFilters_preservesOtherFieldsAndMatches() throws Exception {
        // EP: completion retains status/tag matches and reports the selected contact's original date.
        assertFilteredDone("list st/CLIENT", List.of(nextMonth, nextWeek, zaraOverdue));
        assertFilteredDone("list t/ReFeRrAl", List.of(nextMonth, nextWeek, amyOverdue));
        assertFilteredDone("list st/client t/referral", List.of(nextMonth, nextWeek));
    }

    @Test
    public void execute_listAfterDue_restoresAllContactsInInsertionOrder() throws Exception {
        assertViewThenList("due 7", List.of(oldestOverdue, amyOverdue, zaraOverdue, nextWeek));
    }

    @Test
    public void execute_listAfterFind_restoresAllContactsInInsertionOrder() throws Exception {
        // EP: name-prefix search and phone search, including zero matches.
        assertViewThenList("find ca", List.of(nextWeek));
        assertViewThenList("find p/0005", List.of(amyOverdue));
        assertViewThenList("find Nobody", List.of());
        assertViewThenList("find p/9999", List.of());
    }

    @Test
    public void execute_listAfterFilters_restoresAllContactsInInsertionOrder() throws Exception {
        // EP: status only, tag only, both, and no matching contacts.
        assertViewThenList("list st/CLIENT", List.of(nextMonth, nextWeek, zaraOverdue));
        assertViewThenList("list t/referral", List.of(nextMonth, nextWeek, amyOverdue));
        assertViewThenList("list st/CLIENT t/referral", List.of(nextMonth, nextWeek));
        assertViewThenList("list st/INACTIVE t/vip", List.of());
    }

    @Test
    public void execute_dueWithDefaultAndBoundaryDays_preservesStoredOrder() throws Exception {
        // EP: omitted days and explicit minimum (0) include overdue contacts.
        for (String command : List.of("due", "due 0")) {
            logic.execute(command);
            assertState(originalPersons, List.of(oldestOverdue, amyOverdue, zaraOverdue));
        }

        // Boundary: maximum accepted days (365) includes all dated contacts, excluding missing dates.
        logic.execute("due 365");
        assertState(originalPersons, List.of(oldestOverdue, amyOverdue, zaraOverdue, nextWeek, nextMonth));
    }

    @Test
    public void execute_doneWithSingleDueMatch_savesCompletionAndEmptyView() throws Exception {
        // Boundary: one due match among contacts with no date and a later date.
        initialize(List.of(noFollowUp, nextMonth, nextWeek));
        logic.execute("due 7");
        assertState(List.of(noFollowUp, nextMonth, nextWeek), List.of(nextWeek));

        logic.execute("done 1");
        List<Person> expectedPersons = List.of(noFollowUp, nextMonth, complete(nextWeek));
        assertState(expectedPersons, List.of());
        logic.execute("list");
        assertState(expectedPersons, expectedPersons);
    }

    @Test
    public void execute_mutationsThenReload_preservesDataAndSupportsFurtherCommands() throws Exception {
        // EP: a sequence of different mutations, followed by a fresh model and storage reader.
        logic.execute("due 7");
        logic.execute("done 1");
        List<Person> expectedPersons = replace(originalPersons, oldestOverdue, complete(oldestOverdue));
        assertState(expectedPersons, List.of(amyOverdue, zaraOverdue, nextWeek));

        logic.execute("list st/CLIENT t/referral");
        assertState(expectedPersons, List.of(nextMonth, nextWeek));
        logic.execute("edit 2 e/cara.updated@example.com");
        Person editedPerson = new PersonBuilder(nextWeek).withEmail("cara.updated@example.com").build();
        expectedPersons = replace(expectedPersons, nextWeek, editedPerson);
        // EP: editing an unrelated field preserves the active status/tag filter.
        assertState(expectedPersons, List.of(nextMonth, editedPerson));

        logic.execute("find p/0004");
        assertState(expectedPersons, List.of(zaraOverdue));
        logic.execute("delete 1");
        expectedPersons = remove(expectedPersons, zaraOverdue);
        assertState(expectedPersons, List.of());

        reload();
        assertState(expectedPersons, expectedPersons);
        logic.execute("list st/INACTIVE");
        assertState(expectedPersons, List.of(amyOverdue));
        logic.execute("done 1");
        expectedPersons = replace(expectedPersons, amyOverdue, complete(amyOverdue));
        assertState(expectedPersons, List.of(complete(amyOverdue)));

        reload();
        assertState(expectedPersons, expectedPersons);
    }

    @Test
    public void execute_addThenReload_preservesExplicitAndDefaultFields() throws Exception {
        initialize(List.of());
        // EP: explicit status, multiple tags and a pending date are retained through parsing and storage.
        String followUpDate = nextWeek.getFollowUp().orElseThrow().toString();
        logic.execute("add n/Cara Lee p/81000003 e/cara@example.com a/3 West Road"
                + " st/CLIENT t/referral t/vip f/" + followUpDate);
        assertState(List.of(nextWeek), List.of(nextWeek));

        // EP: omitted status, tags and follow-up use the documented defaults.
        logic.execute("add n/Nora Tan p/81000001 e/nora@example.com a/1 North Road");
        Person defaultPerson = new PersonBuilder(noFollowUp).withTags().build();
        List<Person> expectedPersons = List.of(nextWeek, defaultPerson);
        assertState(expectedPersons, expectedPersons);
        reload();
        assertState(expectedPersons, expectedPersons);
    }

    @Test
    public void execute_addUppercaseNameAbbreviation_preservesNameAndAddress() throws Exception {
        // EP: uppercase A/L and A/P are name text, with the address before or after the name.
        for (String abbreviation : List.of("A/L", "A/P")) {
            String nameField = " n/Ravi " + abbreviation + " Kumar";
            for (String fields : List.of(nameField + " a/1 North Road", " a/1 North Road" + nameField)) {
                initialize(List.of());
                Person expectedPerson = new PersonBuilder(noFollowUp)
                        .withName("Ravi " + abbreviation + " Kumar").build();

                logic.execute("add" + fields + " p/81000001 e/nora@example.com t/family");

                assertState(List.of(expectedPerson), List.of(expectedPerson));
                reload();
                assertState(List.of(expectedPerson), List.of(expectedPerson));
            }
        }
    }

    @Test
    public void execute_editUppercaseNameAbbreviation_changesOnlySuppliedFields() throws Exception {
        for (String abbreviation : List.of("A/L", "A/P")) {
            initialize(List.of(nextWeek));
            Person renamedPerson = new PersonBuilder(nextWeek).withName("Ravi " + abbreviation + " Kumar").build();

            // EP: no address parameter; preserve the address and every other contact field.
            logic.execute("edit 1 n/Ravi " + abbreviation + " Kumar");
            assertState(List.of(renamedPerson), List.of(renamedPerson));
            reload();
            assertState(List.of(renamedPerson), List.of(renamedPerson));

            // EP: an explicit address parameter replaces the address without splitting the name.
            Person movedPerson = new PersonBuilder(renamedPerson).withAddress("20 Tampines Street 2").build();
            logic.execute("edit 1 n/Ravi " + abbreviation + " Kumar a/20 Tampines Street 2");
            assertState(List.of(movedPerson), List.of(movedPerson));
            reload();
            assertState(List.of(movedPerson), List.of(movedPerson));
        }
    }

    @Test
    public void execute_addLowercaseAddressPrefix_splitsNameAndAddress() throws Exception {
        // EP: lowercase a/ starts an address whether the following l or p is lowercase or uppercase.
        for (String suffix : List.of("l", "L", "p", "P")) {
            initialize(List.of());
            Person expectedPerson = new PersonBuilder(noFollowUp).withName("Ravi")
                    .withAddress(suffix + " Kumar").build();

            logic.execute("add n/Ravi a/" + suffix + " Kumar p/81000001 e/nora@example.com t/family");

            assertState(List.of(expectedPerson), List.of(expectedPerson));
            reload();
            assertState(List.of(expectedPerson), List.of(expectedPerson));
        }
    }

    @Test
    public void execute_editLowercaseAddressPrefix_replacesNameAndAddress() throws Exception {
        // EP: lowercase a/ supplies an address update rather than preserving the original address.
        for (String suffix : List.of("l", "L", "p", "P")) {
            initialize(List.of(nextWeek));
            Person expectedPerson = new PersonBuilder(nextWeek).withName("Ravi")
                    .withAddress(suffix + " Kumar").build();

            logic.execute("edit 1 n/Ravi a/" + suffix + " Kumar");

            assertState(List.of(expectedPerson), List.of(expectedPerson));
            reload();
            assertState(List.of(expectedPerson), List.of(expectedPerson));
        }
    }

    @Test
    public void execute_lowercaseNameAbbreviationAndAddress_rejectsDuplicatePrefix() throws Exception {
        byte[] originalFile = Files.readAllBytes(dataFile);
        // EP: a lowercase abbreviation followed by another address exceeds the one-address-prefix limit.
        for (String suffix : List.of("l", "L", "p", "P")) {
            for (String command : List.of(
                    "add n/Ravi a/" + suffix + " Kumar p/92345678 e/ravi@example.com a/1 Road",
                    "edit 1 n/Ravi a/" + suffix + " Kumar a/1 Road")) {
                ParseException exception = assertThrows(ParseException.class, () -> logic.execute(command));
                assertEquals(Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS), exception.getMessage());
                assertState(originalPersons, originalPersons);
                assertArrayEquals(originalFile, Files.readAllBytes(dataFile));
            }
        }
    }

    @Test
    public void execute_unicodeNameThenReload_preservesCanonicalNameAndSearch() throws Exception {
        // EP: composed and decomposed spellings enter through add and remain searchable after reload.
        for (String name : List.of("Jos\u00e9 N\u00fa\u00f1ez", "Jose\u0301 Nu\u0301n\u0303ez")) {
            initialize(List.of());
            Person expectedPerson = new PersonBuilder(noFollowUp).withName("Jos\u00e9 N\u00fa\u00f1ez").build();
            logic.execute("add n/  " + name + "  p/81000001 e/nora@example.com a/1 North Road t/family");
            assertState(List.of(expectedPerson), List.of(expectedPerson));
            reload();
            assertState(List.of(expectedPerson), List.of(expectedPerson));

            // EP: equivalent complete words, mixed-case prefixes, and multiple keywords retain OR matching.
            for (String keywords : List.of("Jos\u00e9", "Jose\u0301", "jOsE\u0301", "NU\u0301N\u0303",
                    "Nobody jOsE\u0301")) {
                logic.execute("find " + keywords);
                assertState(List.of(expectedPerson), List.of(expectedPerson));
            }

            // EP: removing an accent is not canonical equivalence and must not broaden the search.
            logic.execute("find Jose");
            assertState(List.of(expectedPerson), List.of());
        }
    }

    @Test
    public void execute_editUnicodeNameThenReload_preservesCanonicalNameAndSearch() throws Exception {
        // EP: either encoding of a replacement name keeps other fields intact through edit and storage.
        for (String name : List.of("\u00c9lodie N\u00fa\u00f1ez", "E\u0301lodie Nu\u0301n\u0303ez")) {
            initialize(List.of(noFollowUp));
            Person expectedPerson = new PersonBuilder(noFollowUp).withName("\u00c9lodie N\u00fa\u00f1ez").build();
            logic.execute("edit 1 n/  " + name + "  ");
            assertState(List.of(expectedPerson), List.of(expectedPerson));
            reload();
            assertState(List.of(expectedPerson), List.of(expectedPerson));

            // Boundary: a prefix containing the first accented letter must match in either encoding.
            for (String keyword : List.of("\u00e9L", "e\u0301L")) {
                logic.execute("find " + keyword);
                assertState(List.of(expectedPerson), List.of(expectedPerson));
            }
        }
    }

    @Test
    public void execute_equivalentUnicodeDuplicate_preservesDataViewAndFile() throws Exception {
        Person existingPerson = new PersonBuilder(nextMonth).withName("Jos\u00e9 N\u00fa\u00f1ez").build();
        initialize(List.of(noFollowUp, existingPerson));
        logic.execute("find Nora");

        // EP: a hidden duplicate still matches after NFC, case and repeated-space normalization.
        for (String name : List.of("JOS\u00c9  N\u00da\u00d1EZ", "JOSE\u0301  NU\u0301N\u0303EZ")) {
            assertFailure("add n/" + name + " p/89999999 e/new@example.com a/7 Test Road", CommandException.class);
            assertFailure("edit 1 n/" + name, CommandException.class);
        }

        // EP: changing only the encoding of the selected contact's own name is not a duplicate.
        logic.execute("list");
        logic.execute("edit 2 n/Jose\u0301 Nu\u0301n\u0303ez");
        assertState(List.of(noFollowUp, existingPerson), List.of(noFollowUp, existingPerson));
    }

    @Test
    public void execute_deleteOnlyContact_savesAndReloadsEmptyAddressBook() throws Exception {
        // Boundary: deleting the sole contact persists an empty collection.
        initialize(List.of(nextWeek));
        logic.execute("list st/CLIENT t/referral");
        logic.execute("delete 1");
        assertState(List.of(), List.of());
        reload();
        assertState(List.of(), List.of());
    }

    @Test
    public void execute_invalidSyntax_preservesDataViewAndFile() throws Exception {
        for (String viewCommand : List.of("list", "due 7", "list st/CLIENT t/referral")) {
            logic.execute(viewCommand);

            // EP: missing/unsupported arguments, invalid fields, and repeated prefixes.
            for (String command : List.of("unknown", "done", "done 1 2", "edit 1", "edit 1 p/abc",
                    "edit 1 st/UNKNOWN", "edit 1 f/31-02-2026", "list st/UNKNOWN", "list t/",
                    "list st/CLIENT st/PROSPECT", "list t/referral t/vip", "list n/Cara",
                    "find", "find p/abc", "due text", "due 7 8")) {
                assertFailure(command, ParseException.class);
            }

            // Boundary: days just outside the permitted range, and a value exceeding the integer range.
            for (String command : List.of("due -1", "due 366", "due 2147483648")) {
                assertFailure(command, ParseException.class);
            }
        }
    }

    @Test
    public void execute_unicodeStatus_preservesDataViewAndFile() throws Exception {
        // EP: rejected commands retain the full list, due ordering, or a combined status/tag filter.
        for (String viewCommand : List.of("list", "due 7", "list st/CLIENT t/referral")) {
            logic.execute(viewCommand);
            List<Person> displayedBefore = List.copyOf(logic.getFilteredPersonList());
            byte[] fileBefore = Files.readAllBytes(dataFile);

            // EP: dotted I, dotless i and long s resemble ASCII status letters but are unsupported.
            for (String status : List.of("CL\u0130ENT", "cl\u0131ent", "PRO\u017FPECT")) {
                for (String command : List.of(
                        "add n/Status Test p/89999999 e/status@example.com a/7 Test Road st/" + status,
                        "edit 1 n/Changed Name st/" + status,
                        "list st/" + status)) {
                    ParseException exception = assertThrows(ParseException.class, () -> logic.execute(command),
                            command);
                    assertEquals(Status.MESSAGE_CONSTRAINTS, exception.getMessage(), command);
                    assertState(originalPersons, displayedBefore);
                    assertArrayEquals(fileBefore, Files.readAllBytes(dataFile), command);
                }
            }
        }
    }

    @Test
    public void execute_mixedCaseStatuses_savesAndFiltersContacts() throws Exception {
        initialize(List.of());
        // EP: each supported ASCII status accepts mixed case and surrounding spaces through add and list.
        for (String status : List.of("pRoSpEcT", "cLiEnT", "iNaCtIvE")) {
            Person expectedPerson = new PersonBuilder().withName("Status Test").withPhone("89999999")
                    .withEmail("status@example.com").withAddress("7 Test Road").withStatus(status).build();
            logic.execute("add n/Status Test p/89999999 e/status@example.com a/7 Test Road st/  " + status + "  ");
            logic.execute("list st/  " + status + "  ");
            assertState(List.of(expectedPerson), List.of(expectedPerson));

            // EP: editing to another supported status removes a contact from the active status filter.
            String editedStatus = status.equals("iNaCtIvE") ? "cLiEnT" : "iNaCtIvE";
            Person editedPerson = new PersonBuilder(expectedPerson).withStatus(editedStatus).build();
            logic.execute("edit 1 st/  " + editedStatus + "  ");
            assertState(List.of(editedPerson), List.of());
            reload();
            assertState(List.of(editedPerson), List.of(editedPerson));
            initialize(List.of());
        }
    }

    @Test
    public void execute_invalidIndexSyntax_preservesDataViewAndFile() throws Exception {
        logic.execute("due 7");
        // EP: non-positive indices, non-numeric indices and integer overflow.
        for (String index : List.of("0", "-1", "text", "2147483648")) {
            assertFailure("done " + index, ParseException.class);
            assertFailure("delete " + index, ParseException.class);
            assertFailure("edit " + index + " p/89999999", ParseException.class);
        }
    }

    @Test
    public void execute_indexOutsideFilteredList_rejectsHiddenContactIndex() throws Exception {
        // Boundary: index 3 exists in storage, but is one beyond the two displayed contacts.
        logic.execute("list st/CLIENT t/referral");
        assertState(originalPersons, List.of(nextMonth, nextWeek));
        assertFailure("done 3", CommandException.class);
        assertFailure("delete 3", CommandException.class);
        assertFailure("edit 3 p/89999999", CommandException.class);

        // Boundary: the same distinction applies to the sorted due view (four of six contacts).
        logic.execute("due 7");
        assertFailure("done 5", CommandException.class);
        assertFailure("delete 5", CommandException.class);
        assertFailure("edit 5 p/89999999", CommandException.class);
    }

    @Test
    public void execute_indexIntoEmptyView_preservesHiddenContacts() throws Exception {
        // Boundary: the first positive index is invalid when the filter matches no contacts.
        logic.execute("list st/INACTIVE t/vip");
        assertState(originalPersons, List.of());
        assertFailure("done 1", CommandException.class);
        assertFailure("delete 1", CommandException.class);
        assertFailure("edit 1 p/89999999", CommandException.class);
    }

    @Test
    public void execute_doneWithoutPendingFollowUp_preservesDataViewAndFile() throws Exception {
        // EP: a valid displayed contact has no pending date.
        logic.execute("list t/family");
        assertState(originalPersons, List.of(noFollowUp, oldestOverdue));
        assertFailure("done 1", CommandException.class);
    }

    @Test
    public void execute_doneAlreadyCompleted_preservesCompletedContact() throws Exception {
        logic.execute("list st/CLIENT t/referral");
        logic.execute("done 2");
        assertState(replace(originalPersons, nextWeek, complete(nextWeek)), List.of(nextMonth, complete(nextWeek)));

        // EP: repeated completion is rejected while the contact remains at the same filtered index.
        assertFailure("done 2", CommandException.class);
    }

    @Test
    public void execute_editToHiddenDuplicate_preservesDataViewAndFile() throws Exception {
        logic.execute("list st/CLIENT t/referral");
        // EP: the duplicate exists outside the displayed results; matching ignores name case.
        assertFailure("edit 1 n/nora tan", CommandException.class);
    }

    @Test
    public void execute_commandsOnEmptyAddressBook_preservesEmptyData() throws Exception {
        initialize(List.of());
        // Boundary: valid queries return zero contacts and remain safe to save and reload.
        for (String command : List.of("due 7", "list", "find Cara", "list st/CLIENT")) {
            logic.execute(command);
            assertState(List.of(), List.of());
        }
        assertFailure("done 1", CommandException.class);
        assertFailure("delete 1", CommandException.class);
        assertFailure("edit 1 p/89999999", CommandException.class);
        reload();
        assertState(List.of(), List.of());
    }

    private void assertFilteredEdit(String filterCommand, List<Person> displayedPersons) throws Exception {
        for (int index : List.of(1, displayedPersons.size())) {
            initialize(originalPersons);
            logic.execute(filterCommand);
            assertState(originalPersons, displayedPersons);
            Person selectedPerson = displayedPersons.get(index - 1);
            Person editedPerson = new PersonBuilder(selectedPerson).withPhone("89999999").build();

            logic.execute("edit " + index + " p/89999999");
            List<Person> expectedPersons = replace(originalPersons, selectedPerson, editedPerson);
            // Editing the phone preserves status/tag matches and their display order.
            assertState(expectedPersons, replace(displayedPersons, selectedPerson, editedPerson));
        }
    }

    private void assertFilteredDelete(String filterCommand, List<Person> displayedPersons) throws Exception {
        for (int index : List.of(1, displayedPersons.size())) {
            initialize(originalPersons);
            logic.execute(filterCommand);
            assertState(originalPersons, displayedPersons);
            Person selectedPerson = displayedPersons.get(index - 1);

            logic.execute("delete " + index);
            assertState(remove(originalPersons, selectedPerson), remove(displayedPersons, selectedPerson));
        }
    }

    private void assertFilteredDone(String filterCommand, List<Person> displayedPersons) throws Exception {
        for (int index : List.of(1, displayedPersons.size())) {
            initialize(originalPersons);
            logic.execute(filterCommand);
            assertState(originalPersons, displayedPersons);
            Person selectedPerson = displayedPersons.get(index - 1);
            Person completedPerson = complete(selectedPerson);

            CommandResult result = logic.execute("done " + index);
            assertEquals(new CommandResult(String.format(DoneCommand.MESSAGE_SUCCESS, selectedPerson.getName(),
                    selectedPerson.getFollowUp().orElseThrow())), result);
            assertState(replace(originalPersons, selectedPerson, completedPerson),
                    replace(displayedPersons, selectedPerson, completedPerson));
        }
    }

    private void assertViewThenList(String viewCommand, List<Person> displayedPersons) throws Exception {
        // Start sorted so a later query must also clear any stale due comparator.
        logic.execute("due 7");
        logic.execute(viewCommand);
        assertState(originalPersons, displayedPersons);

        CommandResult result = logic.execute("list");
        assertEquals(new CommandResult(ListCommand.MESSAGE_SUCCESS), result);
        assertState(originalPersons, originalPersons);
    }

    private void assertFailure(String command, Class<? extends Exception> exceptionType) throws Exception {
        List<Person> storedBefore = List.copyOf(model.getAddressBook().getPersonList());
        List<Person> displayedBefore = List.copyOf(logic.getFilteredPersonList());
        byte[] fileBefore = Files.readAllBytes(dataFile);

        assertThrows(exceptionType, () -> logic.execute(command), command);
        assertState(storedBefore, displayedBefore);
        assertArrayEquals(fileBefore, Files.readAllBytes(dataFile), command + " must not change the saved file");
    }

    private void assertState(List<Person> storedPersons, List<Person> displayedPersons) throws Exception {
        // Person.equals compares all fields, including status, tags and the optional follow-up date.
        assertEquals(storedPersons, model.getAddressBook().getPersonList(), "Complete stored contact values and order");
        assertEquals(displayedPersons, logic.getFilteredPersonList(), "Complete displayed contact values and order");
        ReadOnlyAddressBook savedAddressBook = new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow();
        assertEquals(storedPersons, savedAddressBook.getPersonList(), "Complete contact values read back from JSON");
    }

    private void initialize(List<Person> persons) throws Exception {
        AddressBook addressBook = new AddressBook();
        for (Person person : persons) {
            addressBook.addPerson(person);
        }
        new JsonAddressBookStorage(dataFile).saveAddressBook(addressBook);
        createLogic(addressBook);
    }

    private void reload() throws Exception {
        createLogic(new JsonAddressBookStorage(dataFile).readAddressBook().orElseThrow());
    }

    private void createLogic(ReadOnlyAddressBook addressBook) {
        model = new ModelManager(addressBook, new UserPrefs());
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
        logic = new LogicManager(model, storage);
    }

    private Person complete(Person person) {
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getStatus(), person.getTags(), Optional.empty());
    }

    private List<Person> replace(List<Person> persons, Person originalPerson, Person replacementPerson) {
        List<Person> updatedPersons = new ArrayList<>(persons);
        updatedPersons.set(updatedPersons.indexOf(originalPerson), replacementPerson);
        return updatedPersons;
    }

    private List<Person> remove(List<Person> persons, Person person) {
        List<Person> remainingPersons = new ArrayList<>(persons);
        remainingPersons.remove(person);
        return remainingPersons;
    }
}
