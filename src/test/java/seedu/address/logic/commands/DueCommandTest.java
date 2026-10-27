package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class DueCommandTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Singapore");
    private static final Clock TODAY = Clock.fixed(
            LocalDate.of(2026, 10, 21).atStartOfDay(ZONE).toInstant(), ZONE);

    private static final Person NO_FOLLOW_UP = new PersonBuilder().withName("Alice Tan").build();
    private static final Person NEXT_WEEK = new PersonBuilder().withName("Ben Lim")
            .withFollowUp("28-10-2026").build();
    private static final Person TODAY_FOLLOW_UP = new PersonBuilder().withName("Carl Lee")
            .withFollowUp("21-10-2026").build();
    private static final Person OVERDUE = new PersonBuilder().withName("Dana Goh")
            .withFollowUp("01-10-2026").build();
    private static final Person NEXT_MONTH = new PersonBuilder().withName("Eve Ong")
            .withFollowUp("21-11-2026").build();

    private Model model;

    @BeforeEach
    public void setUp() {
        AddressBook addressBook = new AddressBook();
        for (Person person : List.of(NO_FOLLOW_UP, NEXT_WEEK, TODAY_FOLLOW_UP, OVERDUE, NEXT_MONTH)) {
            addressBook.addPerson(person);
        }
        model = new ModelManager(addressBook, new UserPrefs());
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        // EP: null model violates the command precondition.
        assertThrows(NullPointerException.class, () -> new DueCommand(0, TODAY).execute(null));
    }

    @Test
    public void execute_today_showsOverdueAndTodayEarliestFirst() {
        // EP: follow-ups are before, on or after the cutoff, or absent; only the first two are included.
        // Boundary values: a zero-day window and a follow-up exactly on today's cutoff.
        CommandResult result = new DueCommand(0, TODAY).execute(model);

        assertEquals("2 follow-ups due by 21-10-2026.", result.getFeedbackToUser());
        assertEquals(List.of(OVERDUE, TODAY_FOLLOW_UP), model.getFilteredPersonList());
    }

    @Test
    public void execute_sevenDays_includesLastDay() {
        // EP: a positive window includes overdue and upcoming follow-ups through the cutoff.
        // Boundary value: NEXT_WEEK is exactly on the inclusive seven-day cutoff.
        CommandResult result = new DueCommand(7, TODAY).execute(model);

        assertEquals("3 follow-ups due by 28-10-2026.", result.getFeedbackToUser());
        assertEquals(List.of(OVERDUE, TODAY_FOLLOW_UP, NEXT_WEEK), model.getFilteredPersonList());
    }

    @Test
    public void execute_oneMatch_usesSingular() {
        // EP: exactly one due contact requires singular feedback.
        model.deletePerson(TODAY_FOLLOW_UP);

        CommandResult result = new DueCommand(0, TODAY).execute(model);

        assertEquals("1 follow-up due by 21-10-2026.", result.getFeedbackToUser());
        assertEquals(List.of(OVERDUE), model.getFilteredPersonList());
    }

    @Test
    public void execute_nonEnglishDefaultLocale_usesAsciiDigits() {
        // EP: a default locale whose number format uses non-ASCII digits must not change the message.
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"));
            CommandResult result = new DueCommand(0, TODAY).execute(model);

            assertEquals("2 follow-ups due by 21-10-2026.", result.getFeedbackToUser());
        } finally {
            Locale.setDefault(originalLocale);
        }
    }

    @Test
    public void execute_noneDue_showsEmptyList() {
        // EP: no due contacts remain, although future and undated contacts still exist.
        // Boundary value: zero results uses the no-follow-ups message.
        model.deletePerson(TODAY_FOLLOW_UP);
        model.deletePerson(OVERDUE);

        CommandResult result = new DueCommand(0, TODAY).execute(model);

        assertEquals("No follow-ups due by 21-10-2026.", result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_sameDate_sortsAccentedNamesAlphabetically() {
        // EP: two follow-ups on the same date are ordered by name, including names with accented letters.
        Person zack = new PersonBuilder().withName("Zack Lim").withPhone("91110001")
                .withEmail("zack@example.com").withFollowUp("21-10-2026").build();
        Person emile = new PersonBuilder().withName("Émile Tan").withPhone("91110002")
                .withEmail("emile@example.com").withFollowUp("21-10-2026").build();
        model.deletePerson(TODAY_FOLLOW_UP);
        model.deletePerson(OVERDUE);
        model.addPerson(zack);
        model.addPerson(emile);

        new DueCommand(0, TODAY).execute(model);

        assertEquals(List.of(emile, zack), model.getFilteredPersonList());
    }

    @Test
    public void execute_thenList_restoresOriginalOrder() {
        // EP: list is run after due has applied a filter and sort; the stored order must be restored.
        new DueCommand(365, TODAY).execute(model);
        new ListCommand().execute(model);

        assertEquals(List.of(NO_FOLLOW_UP, NEXT_WEEK, TODAY_FOLLOW_UP, OVERDUE, NEXT_MONTH),
                model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        DueCommand dueToday = new DueCommand(0, TODAY);
        Clock tomorrow = Clock.offset(TODAY, Duration.ofDays(1));

        assertTrue(dueToday.equals(dueToday));
        assertTrue(dueToday.equals(new DueCommand(0, TODAY)));
        assertFalse(dueToday.equals(new DueCommand(7, TODAY)));
        // Same number of days, but "today" is a different day, so the cutoff differs.
        assertFalse(dueToday.equals(new DueCommand(0, tomorrow)));
        assertFalse(dueToday.equals(null));
        assertFalse(dueToday.equals(new ListCommand()));
    }

    @Test
    public void hashCodeMethod() {
        assertEquals(new DueCommand(7, TODAY).hashCode(), new DueCommand(7, TODAY).hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = DueCommand.class.getCanonicalName() + "{days=7}";
        assertEquals(expected, new DueCommand(7, TODAY).toString());
    }
}
