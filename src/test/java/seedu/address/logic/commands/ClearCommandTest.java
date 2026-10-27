package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

public class ClearCommandTest {

    @Test
    public void execute_unconfirmedEmptyAddressBook_showsWarning() {
        // EP: confirmation is absent and the address book is empty; a warning is still required.
        // Boundary value: zero contacts.
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        String expectedMessage = String.format(ClearCommand.MESSAGE_CONFIRM_CLEAR, 0, "contacts");

        assertCommandSuccess(
                new ClearCommand(false),
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_unconfirmedNonEmptyAddressBook_dataUnchanged() {
        // EP: confirmation is absent and contacts exist; the warning must leave them unchanged.
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        int contactCount = model.getAddressBook().getPersonList().size();
        String expectedMessage =
                String.format(ClearCommand.MESSAGE_CONFIRM_CLEAR, contactCount, "contacts");

        assertCommandSuccess(
                new ClearCommand(false),
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_unconfirmedOneContact_usesSingularNoun() {
        Model model = new ModelManager();
        model.addPerson(getTypicalAddressBook().getPersonList().get(0));
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        String expectedMessage = String.format(ClearCommand.MESSAGE_CONFIRM_CLEAR, 1, "contact");
        assertCommandSuccess(new ClearCommand(false), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_confirmedEmptyAddressBook_success() {
        // EP: confirmation is present and the address book is already empty; clearing is still valid.
        // Boundary value: zero contacts.
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(
                new ClearCommand(true),
                model,
                ClearCommand.MESSAGE_SUCCESS,
                expectedModel);
    }

    @Test
    public void execute_confirmedNonEmptyAddressBook_success() {
        // EP: confirmation is present and contacts exist; every contact must be removed.
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(
                new ClearCommand(true),
                model,
                ClearCommand.MESSAGE_SUCCESS,
                expectedModel);
    }
}
