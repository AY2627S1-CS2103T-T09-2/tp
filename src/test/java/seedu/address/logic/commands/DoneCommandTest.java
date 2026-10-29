package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.LogCapture;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests follow-up completion and index validation in {@code DoneCommand}.
 */
public class DoneCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    private final Person completedPerson = new PersonBuilder()
            .withName("John Tan")
            .withPhone("91234567")
            .withEmail("johntan@example.com")
            .withAddress("21 Clementi Ave 3")
            .withStatus("CLIENT")
            .withTags("referral", "lifePolicy")
            .build();

    private final Person personWithFollowUp = new PersonBuilder(completedPerson)
            .withFollowUp("25-12-2099")
            .build();

    // @@author kaseyho
    @Test
    public void execute_validIndexUnfilteredList_completesFollowUp() {
        // EP: a pending follow-up is cleared; feedback retains its date and other contact fields are preserved.
        // Boundary value: index 1 is the first valid index in the unfiltered list.
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        model.setPerson(originalPerson, personWithFollowUp);

        String expectedMessage =
                "Marked follow-up for John Tan as completed (scheduled for 25-12-2099).";

        Model expectedModel =
                new ModelManager(model.getAddressBook(), new UserPrefs());

        expectedModel.setPerson(personWithFollowUp, completedPerson);

        try (LogCapture logs = new LogCapture(DoneCommand.class)) {
            assertCommandSuccess(
                    new DoneCommand(INDEX_FIRST_PERSON),
                    model,
                    expectedMessage,
                    expectedModel);

            logs.assertSingleLog(
                    Level.INFO,
                    "in model",
                    "displayedIndex=1");

            assertNoContactDetails(logs);
        }
    }

    @Test
    public void execute_validIndexFilteredList_completesFollowUp() {
        // EP: a filtered contact has a pending follow-up; feedback identifies that contact and its original date.
        // Displayed index 1 refers to the second stored contact, so stored indexes must not be used.
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());

        model.setPerson(originalPerson, personWithFollowUp);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        String expectedMessage =
                "Marked follow-up for John Tan as completed (scheduled for 25-12-2099).";

        Model expectedModel =
                new ModelManager(model.getAddressBook(), new UserPrefs());

        expectedModel.setPerson(personWithFollowUp, completedPerson);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        try (LogCapture logs = new LogCapture(DoneCommand.class)) {
            assertCommandSuccess(
                    new DoneCommand(INDEX_FIRST_PERSON),
                    model,
                    expectedMessage,
                    expectedModel);

            logs.assertSingleLog(
                    Level.INFO,
                    "displayedIndex=1");

            assertNoContactDetails(logs);
        }
    }

    @Test
    public void execute_validDateBoundaries_reportsOriginalDate() {
        // EP: valid pending dates retain their DD-MM-YYYY representation after completion.
        // Boundary values: the minimum year, a leap day, and the maximum four-digit year.
        for (String date : List.of("01-01-0001", "29-02-2028", "31-12-9999")) {
            Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
            Person scheduledPerson = new PersonBuilder(completedPerson).withFollowUp(date).build();
            model.setPerson(originalPerson, scheduledPerson);

            Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
            expectedModel.setPerson(scheduledPerson, completedPerson);

            String expectedMessage = "Marked follow-up for John Tan as completed (scheduled for " + date + ").";
            assertCommandSuccess(new DoneCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
        }
    }
    // @@author

    @Test
    public void execute_completeFollowUp_preservesInteractionNotes() {
        // EP: a contact has both a pending follow-up and notes; completing it must retain every note.
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        InteractionNote firstNote =
                new InteractionNote("Called client");

        InteractionNote secondNote =
                new InteractionNote("Discussed renewal options");

        Person personWithNotesAndFollowUp =
                new PersonBuilder(completedPerson)
                        .withInteractionNotes(
                                firstNote,
                                secondNote)
                        .withFollowUp("25-12-2099")
                        .build();

        Person expectedPerson =
                new PersonBuilder(completedPerson)
                        .withInteractionNotes(
                                firstNote,
                                secondNote)
                        .build();

        model.setPerson(
                originalPerson,
                personWithNotesAndFollowUp);

        String expectedMessage =
                "Marked follow-up for John Tan as completed (scheduled for 25-12-2099).";

        Model expectedModel =
                new ModelManager(
                        model.getAddressBook(),
                        new UserPrefs());

        expectedModel.setPerson(
                personWithNotesAndFollowUp,
                expectedPerson);

        assertCommandSuccess(
                new DoneCommand(INDEX_FIRST_PERSON),
                model,
                expectedMessage,
                expectedModel);
    }

    // @@author kaseyho
    @Test
    public void execute_noPendingFollowUp_throwsCommandException() {
        // EP: displayed contact has no pending follow-up, so there is nothing to complete.
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        model.setPerson(originalPerson, completedPerson);

        try (LogCapture logs = new LogCapture(DoneCommand.class)) {
            assertCommandFailure(
                    new DoneCommand(INDEX_FIRST_PERSON),
                    model,
                    "John Tan does not have a pending follow-up.");

            logs.assertSingleLog(
                    Level.FINE,
                    "no pending follow-up",
                    "displayedIndex=1");

            assertNoContactDetails(logs);
        }
    }

    @Test
    public void execute_followUpAlreadyCompleted_throwsCommandException() {
        // EP: no pending follow-up, reached by repeating a successful completion.
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        model.setPerson(originalPerson, personWithFollowUp);

        Model expectedModel =
                new ModelManager(model.getAddressBook(), new UserPrefs());

        expectedModel.setPerson(personWithFollowUp, completedPerson);

        DoneCommand command =
                new DoneCommand(INDEX_FIRST_PERSON);

        assertCommandSuccess(
                command,
                model,
                "Marked follow-up for John Tan as completed (scheduled for 25-12-2099).",
                expectedModel);

        assertCommandFailure(
                command,
                model,
                "John Tan does not have a pending follow-up.");
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        // EP: index exceeds the displayed list size.
        // Boundary value: size + 1 is the first invalid index above the unfiltered list.
        Index invalidIndex =
                Index.fromOneBased(
                        model.getFilteredPersonList().size() + 1);

        try (LogCapture logs = new LogCapture(DoneCommand.class)) {
            assertCommandFailure(
                    new DoneCommand(invalidIndex),
                    model,
                    // @@author
                    // @@author kqjy29
                    Messages.getInvalidPersonDisplayedIndexMessage(
                            invalidIndex.getOneBased(),
                            model.getFilteredPersonList().size()));
            // @@author
            // @@author kaseyho

            logs.assertSingleLog(
                    Level.FINE,
                    "displayedIndex=" + invalidIndex.getOneBased(),
                    "displayedCount=" + model.getFilteredPersonList().size());
        }
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        // EP: index exists in storage but is outside the filtered list.
        // Boundary value: index 2 is just above the one-contact displayed list.
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        try (LogCapture logs = new LogCapture(DoneCommand.class)) {
            assertCommandFailure(
                    new DoneCommand(INDEX_SECOND_PERSON),
                    model,
                    // @@author
                    // @@author kqjy29
                    Messages.getInvalidPersonDisplayedIndexMessage(
                            INDEX_SECOND_PERSON.getOneBased(),
                            model.getFilteredPersonList().size()));
            // @@author
            // @@author kaseyho

            logs.assertSingleLog(
                    Level.FINE,
                    "displayedIndex=2",
                    "displayedCount=1");
        }
    }

    @Test
    public void execute_modelUpdateFails_doesNotLogCompletion() {
        // EP: an otherwise valid completion is rejected by the model; success must not be logged.
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        model.setPerson(originalPerson, personWithFollowUp);

        Model rejectingModel =
                new ModelManager(model.getAddressBook(), new UserPrefs()) {
                    @Override
                    public void setPerson(
                            Person target,
                            Person editedPerson) {
                        throw new IllegalArgumentException(
                                "Model update failed");
                    }
                };

        try (LogCapture logs = new LogCapture(DoneCommand.class)) {
            assertThrows(IllegalArgumentException.class, "Model update failed", () -> {
                new DoneCommand(INDEX_FIRST_PERSON).execute(rejectingModel);
            });

            assertTrue(logs.getRecords().isEmpty());
            assertEquals(
                    model.getAddressBook(),
                    rejectingModel.getAddressBook());
        }
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        // EP: null model violates the command precondition.
        assertThrows(NullPointerException.class, () ->
                new DoneCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals_sameAndDifferentIndexes_returnsExpectedResult() {
        DoneCommand firstCommand =
                new DoneCommand(INDEX_FIRST_PERSON);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(
                firstCommand.equals(
                        new DoneCommand(Index.fromOneBased(1))));
        assertFalse(
                firstCommand.equals(
                        new DoneCommand(INDEX_SECOND_PERSON)));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(new Object()));
        // @@author

        assertEquals(
                firstCommand.hashCode(),
                new DoneCommand(Index.fromOneBased(1)).hashCode());
    }

    // @@author kaseyho
    private void assertNoContactDetails(LogCapture logs) {
        String message =
                logs.getRecords().getFirst().getMessage();

        assertFalse(message.contains("John Tan"));
        assertFalse(message.contains("91234567"));
        assertFalse(message.contains("johntan@example.com"));
        assertFalse(message.contains("21 Clementi Ave 3"));
        assertFalse(message.contains("referral"));
        assertFalse(message.contains("lifePolicy"));
        assertFalse(message.contains("25-12-2099"));
    }
    // @@author
}
