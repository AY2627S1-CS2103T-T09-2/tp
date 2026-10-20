package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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
        assertThrows(NullPointerException.class, () -> new DueCommand(0, TODAY).execute(null));
    }

    @Test
    public void execute_today_showsOverdueAndTodayEarliestFirst() {
        CommandResult result = new DueCommand(0, TODAY).execute(model);

        assertEquals("2 follow-ups due by 21-10-2026.", result.getFeedbackToUser());
        assertEquals(List.of(OVERDUE, TODAY_FOLLOW_UP), model.getFilteredPersonList());
    }

    @Test
    public void execute_sevenDays_includesLastDay() {
        CommandResult result = new DueCommand(7, TODAY).execute(model);

        assertEquals("3 follow-ups due by 28-10-2026.", result.getFeedbackToUser());
        assertEquals(List.of(OVERDUE, TODAY_FOLLOW_UP, NEXT_WEEK), model.getFilteredPersonList());
    }

    @Test
    public void execute_oneMatch_usesSingular() {
        model.deletePerson(TODAY_FOLLOW_UP);

        CommandResult result = new DueCommand(0, TODAY).execute(model);

        assertEquals("1 follow-up due by 21-10-2026.", result.getFeedbackToUser());
        assertEquals(List.of(OVERDUE), model.getFilteredPersonList());
    }

    @Test
    public void execute_noneDue_showsEmptyList() {
        model.deletePerson(TODAY_FOLLOW_UP);
        model.deletePerson(OVERDUE);

        CommandResult result = new DueCommand(0, TODAY).execute(model);

        assertEquals("No follow-ups due by 21-10-2026.", result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_thenList_restoresOriginalOrder() {
        new DueCommand(365, TODAY).execute(model);
        new ListCommand().execute(model);

        assertEquals(List.of(NO_FOLLOW_UP, NEXT_WEEK, TODAY_FOLLOW_UP, OVERDUE, NEXT_MONTH),
                model.getFilteredPersonList());
    }

    @Test
    public void equals() {
        DueCommand dueToday = new DueCommand(0, TODAY);

        assertTrue(dueToday.equals(dueToday));
        assertTrue(dueToday.equals(new DueCommand(0, TODAY)));
        assertFalse(dueToday.equals(new DueCommand(7, TODAY)));
        assertFalse(dueToday.equals(null));
        assertFalse(dueToday.equals(new ListCommand()));
    }
}
