package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.LastContact;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class ContactedCommandTest {

    private final Model model =
            new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_updatesLastContact() {
        Person personToUpdate =
                model.getFilteredPersonList()
                        .get(INDEX_FIRST_PERSON.getZeroBased());

        LastContact lastContact =
                new LastContact("27-10-2026");

        ContactedCommand command =
                new ContactedCommand(
                        INDEX_FIRST_PERSON,
                        lastContact);

        Person updatedPerson =
                new PersonBuilder(personToUpdate)
                        .withLastContact("27-10-2026")
                        .build();

        Model expectedModel =
                new ModelManager(
                        new AddressBook(model.getAddressBook()),
                        new UserPrefs());

        expectedModel.setPerson(
                personToUpdate,
                updatedPerson);

        String expectedMessage =
                String.format(
                        ContactedCommand.MESSAGE_SUCCESS,
                        personToUpdate.getName(),
                        lastContact);

        assertCommandSuccess(
                command,
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex =
                Index.fromOneBased(
                        model.getFilteredPersonList().size() + 1);

        ContactedCommand command =
                new ContactedCommand(
                        invalidIndex,
                        new LastContact("27-10-2026"));

        String expectedMessage =
                Messages.getInvalidPersonDisplayedIndexMessage(
                        invalidIndex.getOneBased(),
                        model.getFilteredPersonList().size());

        assertCommandFailure(
                command,
                model,
                expectedMessage);
    }
}
