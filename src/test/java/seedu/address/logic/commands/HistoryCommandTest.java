package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class HistoryCommandTest {

    private final Model model =
            new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_personWithoutNotes_returnsNoNotesMessage() {
        Person person =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage =
                String.format(
                        HistoryCommand.MESSAGE_NO_NOTES,
                        person.getName());

        assertCommandSuccess(
                new HistoryCommand(INDEX_FIRST_PERSON),
                model,
                expectedMessage,
                model);
    }

    @Test
    public void execute_personWithNotes_returnsInteractionHistory() {
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        Person personWithNotes =
                new PersonBuilder(originalPerson)
                        .withInteractionNotes(
                                "First meeting",
                                "Discussed hospital coverage")
                        .build();

        model.setPerson(originalPerson, personWithNotes);

        String expectedMessage =
                String.format(
                        HistoryCommand.MESSAGE_HISTORY_HEADER,
                        personWithNotes.getName())
                        + System.lineSeparator()
                        + "1. First meeting"
                        + System.lineSeparator()
                        + "2. Discussed hospital coverage";

        assertCommandSuccess(
                new HistoryCommand(INDEX_FIRST_PERSON),
                model,
                expectedMessage,
                model);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex =
                Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        String expectedMessage =
                Messages.getInvalidPersonDisplayedIndexMessage(
                        invalidIndex.getOneBased(),
                        model.getFilteredPersonList().size());

        assertCommandFailure(
                new HistoryCommand(invalidIndex),
                model,
                expectedMessage);
    }

    @Test
    public void equals() {
        HistoryCommand firstCommand =
                new HistoryCommand(INDEX_FIRST_PERSON);

        HistoryCommand sameCommand =
                new HistoryCommand(INDEX_FIRST_PERSON);

        HistoryCommand differentCommand =
                new HistoryCommand(INDEX_SECOND_PERSON);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(sameCommand));
        assertFalse(firstCommand.equals(differentCommand));
        assertFalse(firstCommand.equals(null));
    }
}
