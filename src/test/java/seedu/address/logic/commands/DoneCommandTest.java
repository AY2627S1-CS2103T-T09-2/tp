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

/**
 * Tests index validation and unchanged contact data in {@code DoneCommand}.
 */
public class DoneCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_leavesContactsUnchanged() {
        Person selectedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = selectedPerson.getName()
                + " selected; completing follow-ups is coming soon.";
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        assertCommandSuccess(new DoneCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_leavesContactsUnchanged() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person selectedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        String expectedMessage = selectedPerson.getName()
                + " selected; completing follow-ups is coming soon.";
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        assertCommandSuccess(new DoneCommand(INDEX_FIRST_PERSON), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index invalidIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        assertCommandFailure(new DoneCommand(invalidIndex), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        assertCommandFailure(new DoneCommand(INDEX_SECOND_PERSON), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
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
