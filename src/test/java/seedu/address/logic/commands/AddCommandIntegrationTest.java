package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        // EP: the contact has an identity not already present in the real model.
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        int displayedIndex = expectedModel.getFilteredPersonList().indexOf(validPerson);
        String expectedMessage = String.format(AddCommand.MESSAGE_SUCCESS,
                validPerson.getName(), validPerson.getStatus())
                + String.format(AddCommand.MESSAGE_DISPLAYED_INDEX, displayedIndex + 1);
        assertCommandSuccess(new AddCommand(validPerson), model,
                new CommandResult(expectedMessage, displayedIndex), expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        // EP: the contact identity already exists; adding it must leave the model unchanged.
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON,
                        personInList.getName(), personInList.getPhone()));
    }

}
