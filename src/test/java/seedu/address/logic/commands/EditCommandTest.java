package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Status;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_editPhone_preservesStatus() throws Exception {
        // EP: status is omitted from a partial edit; the existing status must be retained.
        Person client = new PersonBuilder(ALICE).withStatus("CLIENT").build();
        model.setPerson(ALICE, client);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();

        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertEquals(Status.CLIENT, model.getFilteredPersonList().get(0).getStatus());
    }

    @Test
    public void execute_editStatus_success() throws Exception {
        // EP: status is explicitly supplied; it must replace the existing status.
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withStatus("CLIENT")
                .build();

        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertEquals(Status.CLIENT, model.getFilteredPersonList().get(0).getStatus());
    }

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        // EP: name, phone, email, address, status and tags are all supplied for a unique replacement.
        // Boundary value: index 1 is the first valid unfiltered index.
        Person editedPerson = new PersonBuilder().build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editPersonWithFollowUp_followUpKept() {
        // EP: follow-up is omitted from a partial edit; the existing date must be retained.
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person firstPersonWithFollowUp = new PersonBuilder(firstPerson).withFollowUp("25-12-2099").build();
        model.setPerson(firstPerson, firstPersonWithFollowUp);

        Person editedPerson = new PersonBuilder(firstPersonWithFollowUp).withPhone(VALID_PHONE_BOB).build();
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(firstPersonWithFollowUp, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editPersonWithInteractionNotes_notesKept() {
        // EP: a contact has existing notes; editing its phone must retain every note.
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        Person firstPersonWithNotes = new PersonBuilder(firstPerson)
                .withInteractionNotes(
                        "Met client",
                        "Discussed hospital coverage")
                .build();

        model.setPerson(firstPerson, firstPersonWithNotes);

        Person editedPerson = new PersonBuilder(firstPersonWithNotes)
                .withPhone(VALID_PHONE_BOB)
                .build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withPhone(VALID_PHONE_BOB)
                .build();

        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(
                EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson));

        Model expectedModel = new ModelManager(
                new AddressBook(model.getAddressBook()),
                new UserPrefs());

        expectedModel.setPerson(firstPersonWithNotes, editedPerson);

        assertCommandSuccess(
                editCommand,
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_editFollowUp_success() throws Exception {
        // EP: a future follow-up date is explicitly supplied to a contact with no existing date.
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withFollowUp("25-12-2099")
                .build();

        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertEquals("25-12-2099", model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased()).getFollowUp().get().toString());
    }

    @Test
    public void execute_clearFollowUp_success() throws Exception {
        // EP: follow-up is explicitly cleared, which differs from omitting the field.
        Person person = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithFollowUp = new PersonBuilder(person).withFollowUp("25-12-2099").build();
        model.setPerson(person, personWithFollowUp);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withFollowUp().build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(model);

        assertTrue(model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased())
                .getFollowUp().isEmpty());
    }

    @Test
    public void execute_editPastFollowUp_showsWarning() throws Exception {
        // EP: a past follow-up date is explicitly supplied; it is accepted with a warning.
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withFollowUp("01-01-2020")
                .build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withFollowUp("01-01-2020").build();

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS,
                Messages.format(editedPerson)) + editedPerson.getFollowUp().get().getPastDateWarning();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        // EP: only some contact fields are supplied; unspecified fields must be retained.
        // Boundary value: the last displayed index is the upper valid index.
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB)
                .withTags(VALID_TAG_HUSBAND).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).withTags(VALID_TAG_HUSBAND).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        // EP: an empty descriptor leaves the contact unchanged when the command is called directly.
        // The parser rejects this user input; this test isolates command behavior.
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() {
        // EP: a valid index refers to a contact in the filtered list.
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        // EP: the edited identity would duplicate another contact in the unfiltered list.
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, String.format(EditCommand.MESSAGE_DUPLICATE_PERSON,
                firstPerson.getName(), firstPerson.getPhone()));
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        // EP: the edited identity would duplicate a stored contact hidden by the current filter.
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, String.format(EditCommand.MESSAGE_DUPLICATE_PERSON,
                personInList.getName(), personInList.getPhone()));
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        // EP: the index exceeds the unfiltered list size.
        // Boundary value: size + 1 is the first invalid index above the list.
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model,
                Messages.getInvalidPersonDisplayedIndexMessage(outOfBoundIndex.getOneBased(),
                        model.getFilteredPersonList().size()));
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        // EP: the index exists in storage but is outside the filtered list.
        // Boundary value: index 2 is just above the one-contact displayed list.
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model,
                Messages.getInvalidPersonDisplayedIndexMessage(outOfBoundIndex.getOneBased(),
                        model.getFilteredPersonList().size()));
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand(false)));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }
}
