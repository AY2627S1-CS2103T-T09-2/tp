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

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
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

    @Test
    public void execute_validIndexUnfilteredList_completesFollowUp() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(originalPerson, personWithFollowUp);

        String expectedMessage = "Marked follow-up for John Tan as completed.";
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithFollowUp, completedPerson);

        assertCommandSuccess(new DoneCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_completesFollowUp() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        model.setPerson(originalPerson, personWithFollowUp);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        String expectedMessage = "Marked follow-up for John Tan as completed.";
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithFollowUp, completedPerson);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertCommandSuccess(new DoneCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noPendingFollowUp_throwsCommandException() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(originalPerson, completedPerson);

        assertCommandFailure(new DoneCommand(INDEX_FIRST_PERSON), model,
                "John Tan does not have a pending follow-up.");
    }

    @Test
    public void execute_followUpAlreadyCompleted_throwsCommandException() {
        Person originalPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        model.setPerson(originalPerson, personWithFollowUp);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personWithFollowUp, completedPerson);
        DoneCommand command = new DoneCommand(INDEX_FIRST_PERSON);

        assertCommandSuccess(command, model, "Marked follow-up for John Tan as completed.", expectedModel);
        assertCommandFailure(command, model, "John Tan does not have a pending follow-up.");
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new DoneCommand(invalidIndex), model,
                Messages.getInvalidPersonDisplayedIndexMessage(invalidIndex.getOneBased(),
                        model.getFilteredPersonList().size()));
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new DoneCommand(INDEX_SECOND_PERSON), model,
                Messages.getInvalidPersonDisplayedIndexMessage(INDEX_SECOND_PERSON.getOneBased(),
                        model.getFilteredPersonList().size()));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DoneCommand(INDEX_FIRST_PERSON).execute(null));
    }

    @Test
    public void equals_sameAndDifferentIndexes_returnsExpectedResult() {
        DoneCommand firstCommand = new DoneCommand(INDEX_FIRST_PERSON);

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(new DoneCommand(Index.fromOneBased(1))));
        assertFalse(firstCommand.equals(new DoneCommand(INDEX_SECOND_PERSON)));
        assertFalse(firstCommand.equals(null));
        assertFalse(firstCommand.equals(new Object()));
        assertEquals(firstCommand.hashCode(), new DoneCommand(Index.fromOneBased(1)).hashCode());
    }
}
