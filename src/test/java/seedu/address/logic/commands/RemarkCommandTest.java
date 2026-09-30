package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
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
import seedu.address.model.person.Remark;

public class RemarkCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_addAndRemoveRemark_success() throws Exception {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball")).execute(model);
        Person withRemark = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertEquals(new Remark("Likes baseball"), withRemark.getRemark());
        assertEquals(original.getName(), withRemark.getName());
        assertEquals(original.getTags(), withRemark.getTags());

        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")).execute(model);
        assertEquals(original, model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased()));
    }

    @Test
    public void execute_filteredList_usesDisplayedIndex() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person selected = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Follow up")).execute(model);

        Person updated = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.isSamePerson(selected)).findFirst().orElseThrow();
        assertEquals(new Remark("Follow up"), updated.getRemark());
        assertTrue(model.getFilteredPersonList().size() > 1);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index outOfBounds = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        assertCommandFailure(new RemarkCommand(outOfBounds, new Remark("Hi")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }
}
