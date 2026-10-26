package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandTest {

    private final Model model =
            new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_addsInteractionNote() {
        Person personToUpdate =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        InteractionNote note =
                new InteractionNote("Discussed hospital coverage");

        NoteCommand command =
                new NoteCommand(INDEX_FIRST_PERSON, note);

        Person updatedPerson =
                new PersonBuilder(personToUpdate)
                        .withInteractionNotes(note)
                        .build();

        Model expectedModel =
                new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        expectedModel.setPerson(personToUpdate, updatedPerson);

        String expectedMessage =
                String.format(
                        NoteCommand.MESSAGE_SUCCESS,
                        personToUpdate.getName());

        assertCommandSuccess(
                command,
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_existingNotes_appendsInteractionNote() {
        Person originalPerson =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        InteractionNote existingNote =
                new InteractionNote("First meeting");

        InteractionNote newNote =
                new InteractionNote("Second meeting");

        Person personWithExistingNote =
                new PersonBuilder(originalPerson)
                        .withInteractionNotes(existingNote)
                        .build();

        model.setPerson(originalPerson, personWithExistingNote);

        NoteCommand command =
                new NoteCommand(
                        INDEX_FIRST_PERSON,
                        newNote);

        Person expectedPerson =
                new PersonBuilder(personWithExistingNote)
                        .withInteractionNotes(
                                existingNote,
                                newNote)
                        .build();

        Model expectedModel =
                new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        expectedModel.setPerson(personWithExistingNote, expectedPerson);

        String expectedMessage =
                String.format(
                        NoteCommand.MESSAGE_SUCCESS,
                        personWithExistingNote.getName());

        assertCommandSuccess(
                command,
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_addsInteractionNote() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        Person personToUpdate =
                model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        InteractionNote note =
                new InteractionNote("Called client");

        NoteCommand command =
                new NoteCommand(
                        INDEX_FIRST_PERSON,
                        note);

        Person updatedPerson =
                new PersonBuilder(personToUpdate)
                        .withInteractionNotes(note)
                        .build();

        Model expectedModel =
                new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        expectedModel.setPerson(personToUpdate, updatedPerson);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);

        String expectedMessage =
                String.format(
                        NoteCommand.MESSAGE_SUCCESS,
                        personToUpdate.getName());

        assertCommandSuccess(
                command,
                model,
                expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        Index invalidIndex =
                Index.fromOneBased(model.getFilteredPersonList().size() + 1);

        NoteCommand command =
                new NoteCommand(
                        invalidIndex,
                        new InteractionNote("Meeting"));

        String expectedMessage =
                Messages.getInvalidPersonDisplayedIndexMessage(
                        invalidIndex.getOneBased(),
                        model.getFilteredPersonList().size());

        assertCommandFailure(
                command,
                model,
                expectedMessage);
    }

    @Test
    public void equals() {
        NoteCommand firstCommand =
                new NoteCommand(
                        INDEX_FIRST_PERSON,
                        new InteractionNote("Meeting"));

        NoteCommand sameCommand =
                new NoteCommand(
                        INDEX_FIRST_PERSON,
                        new InteractionNote("Meeting"));

        NoteCommand differentIndex =
                new NoteCommand(
                        INDEX_SECOND_PERSON,
                        new InteractionNote("Meeting"));

        NoteCommand differentNote =
                new NoteCommand(
                        INDEX_FIRST_PERSON,
                        new InteractionNote("Phone call"));

        assertTrue(firstCommand.equals(firstCommand));
        assertTrue(firstCommand.equals(sameCommand));
        assertFalse(firstCommand.equals(differentIndex));
        assertFalse(firstCommand.equals(differentNote));
        assertFalse(firstCommand.equals(null));
    }
}
