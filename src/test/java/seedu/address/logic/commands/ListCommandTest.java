package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.LogCapture;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests list results, feedback and preservation of stored contacts.
 */
public class ListCommandTest {

    private static final Person CLIENT_REFERRAL = new PersonBuilder().withName("Alice Tan")
            .withStatus("CLIENT").withTags("ReFeRRaL").withFollowUp("25-12-2099").build();
    private static final Person CLIENT_FAMILY = new PersonBuilder().withName("Ben Lim")
            .withStatus("CLIENT").withTags("family").build();
    private static final Person PROSPECT_REFERRAL = new PersonBuilder().withName("Carl Lee")
            .withStatus("PROSPECT").withTags("referral").build();
    private static final Person INACTIVE_WITHOUT_TAGS = new PersonBuilder().withName("Dana Goh")
            .withStatus("INACTIVE").build();

    private Model model;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        for (Person person : List.of(CLIENT_REFERRAL, CLIENT_FAMILY,
                PROSPECT_REFERRAL, INACTIVE_WITHOUT_TAGS)) {
            addressBook.addPerson(person);
        }
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        // EP: null model violates the command precondition.
        assertThrows(NullPointerException.class, () -> new ListCommand().execute(null));
    }

    @Test
    public void execute_noFilters_showsAllContacts() {
        // EP: neither filter is supplied; all statuses and contacts with or without tags are included.
        try (LogCapture logs = new LogCapture(ListCommand.class)) {
            assertListResult(new ListCommand(), "Listed all contacts.",
                    List.of(CLIENT_REFERRAL, CLIENT_FAMILY, PROSPECT_REFERRAL, INACTIVE_WITHOUT_TAGS));
            logs.assertSingleLog(Level.INFO, "status=ALL", "tagFilter=false", "resultCount=4");
        }
    }

    @Test
    public void execute_noFiltersAfterFiltering_showsAllContacts() {
        // EP: neither filter is supplied while a previous filter is active; that filter must be cleared.
        model.updateFilteredPersonList(person -> person.equals(PROSPECT_REFERRAL));

        assertListResult(new ListCommand(), "Listed all contacts.",
                List.of(CLIENT_REFERRAL, CLIENT_FAMILY, PROSPECT_REFERRAL, INACTIVE_WITHOUT_TAGS));
    }

    @Test
    public void execute_statusFilter_showsMatchingContacts() {
        // EP: only status is supplied; matching contacts may have different tags.
        // Two matches represent the plural-feedback partition.
        ListCommand command = new ListCommand(Optional.of(Status.CLIENT), Optional.empty());

        assertListResult(command, "Listed 2 contacts with status CLIENT.",
                List.of(CLIENT_REFERRAL, CLIENT_FAMILY));
    }

    @Test
    public void execute_tagFilter_matchesIgnoringCase() {
        // EP: only a tag is supplied; tag case and contact status must not affect matching.
        ListCommand command = new ListCommand(Optional.empty(), Optional.of(new Tag("REFERRAL")));

        assertListResult(command, "Listed 2 contacts with tag referral.",
                List.of(CLIENT_REFERRAL, PROSPECT_REFERRAL));
    }

    @Test
    public void execute_bothFilters_showsIntersection() {
        // EP: both filters are supplied; matching only one condition is insufficient.
        // One match represents the singular-feedback partition.
        ListCommand command =
                new ListCommand(Optional.of(Status.CLIENT), Optional.of(new Tag("referral")));

        try (LogCapture logs = new LogCapture(ListCommand.class)) {
            assertListResult(command, "Listed 1 contact with status CLIENT and tag referral.",
                    List.of(CLIENT_REFERRAL));
            logs.assertSingleLog(Level.INFO, "status=CLIENT", "tagFilter=true", "resultCount=1");
            String message = logs.getRecords().getFirst().getMessage();
            assertFalse(message.contains("Alice Tan"));
            assertFalse(message.toLowerCase(Locale.ROOT).contains("referral"));
            assertFalse(message.contains(CLIENT_REFERRAL.getPhone().value));
            assertFalse(message.contains(CLIENT_REFERRAL.getEmail().value));
            assertFalse(message.contains(CLIENT_REFERRAL.getAddress().value));
        }
    }

    @Test
    public void execute_noMatchingContacts_showsEmptyList() {
        // EP: a valid filter matches no contacts in a nonempty address book.
        // Boundary value: zero results, rather than an invalid filter or an execution error.
        ListCommand command = new ListCommand(Optional.empty(), Optional.of(new Tag("missing")));

        try (LogCapture logs = new LogCapture(ListCommand.class)) {
            assertListResult(command, "Listed 0 contacts with tag missing.", List.of());
            logs.assertSingleLog(Level.INFO, "status=ALL", "tagFilter=true", "resultCount=0");
            assertFalse(logs.getRecords().getFirst().getMessage().contains("missing"));
        }
    }

    @Test
    public void execute_emptyAddressBook_showsEmptyList() {
        // EP: the address book has no contacts, with or without a filter.
        // Boundary value: zero stored contacts.
        model = new ModelManager(new AddressBook(), new UserPrefs());
        assertListResult(new ListCommand(), "Listed all contacts.", List.of());
        ListCommand command = new ListCommand(Optional.of(Status.CLIENT), Optional.empty());

        assertListResult(command, "Listed 0 contacts with status CLIENT.", List.of());
    }

    @Test
    public void execute_previousFilterIsActive_replacesPreviousFilter() {
        // EP: a new filter replaces an active filter; it must not narrow only the previous results.
        model.updateFilteredPersonList(person -> person.equals(PROSPECT_REFERRAL));
        ListCommand command = new ListCommand(Optional.of(Status.CLIENT), Optional.empty());

        assertListResult(command, "Listed 2 contacts with status CLIENT.",
                List.of(CLIENT_REFERRAL, CLIENT_FAMILY));
    }

    private void assertListResult(ListCommand command, String expectedMessage, List<Person> expectedPersons) {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());

        assertEquals(new CommandResult(expectedMessage), command.execute(model));
        assertEquals(expectedPersons, model.getFilteredPersonList());
        assertEquals(originalAddressBook, model.getAddressBook());
    }
}
