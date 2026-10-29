package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class DeleteNoteCommandTest {

    @Test
    public void execute_deleteDisplayedNote_usesInsertionOrder()
            throws CommandException {

        // EP: notes are ordered by insertion rather than their local timestamps.
        // Simulates a time-zone change where the later-added note has an earlier-looking timestamp.
        InteractionNote firstNote = new InteractionNote(
                "Discussed hospital coverage",
                LocalDateTime.of(2026, 10, 26, 21, 10));

        InteractionNote secondNote = new InteractionNote(
                "Client requested a quotation",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        Person person = new PersonBuilder()
                .withName("John Tan")
                .withInteractionNotes(firstNote, secondNote)
                .build();

        Model model = new ModelManager();
        model.addPerson(person);

        DeleteNoteCommand command = new DeleteNoteCommand(
                Index.fromOneBased(1),
                Index.fromOneBased(1));

        CommandResult result = command.execute(model);

        Person updatedPerson =
                model.getFilteredPersonList().get(0);

        assertEquals(
                "Deleted interaction note 1 for John Tan.",
                result.getFeedbackToUser());

        assertEquals(person.getName(), updatedPerson.getName());
        assertEquals(person.getPhone(), updatedPerson.getPhone());
        assertEquals(person.getEmail(), updatedPerson.getEmail());
        assertEquals(person.getAddress(), updatedPerson.getAddress());
        assertEquals(person.getStatus(), updatedPerson.getStatus());
        assertEquals(person.getTags(), updatedPerson.getTags());
        assertEquals(person.getFollowUp(), updatedPerson.getFollowUp());

        assertEquals(
                List.of(firstNote),
                updatedPerson.getInteractionNotes());
    }

    @Test
    public void execute_equalNotes_deletesDisplayedPosition()
            throws CommandException {

        // EP: two notes can compare equal but occupy different stored positions.
        // The newest displayed occurrence must be removed, not the first equal stored note.
        InteractionNote sameNote = new InteractionNote(
                "Same",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        InteractionNote otherNote = new InteractionNote(
                "Other",
                LocalDateTime.of(2026, 10, 26, 21, 10));

        Person person = new PersonBuilder()
                .withName("John Tan")
                .withInteractionNotes(sameNote, otherNote, sameNote)
                .build();

        Model model = new ModelManager();
        model.addPerson(person);

        DeleteNoteCommand command = new DeleteNoteCommand(
                Index.fromOneBased(1),
                Index.fromOneBased(1));

        command.execute(model);

        Person updatedPerson =
                model.getFilteredPersonList().get(0);

        assertEquals(
                List.of(sameNote, otherNote),
                updatedPerson.getInteractionNotes());
    }

    @Test
    public void execute_invalidNoteIndexWithOneNote_usesSingularNote()
            throws CommandException {

        // EP: the requested note index is outside a one-note history.
        // Boundary value: one stored note requires the singular noun in the error.
        InteractionNote note = new InteractionNote(
                "Called client",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        Person person = new PersonBuilder()
                .withName("John Tan")
                .withInteractionNotes(note)
                .build();

        Model model = new ModelManager();
        model.addPerson(person);

        DeleteNoteCommand command = new DeleteNoteCommand(
                Index.fromOneBased(1),
                Index.fromOneBased(2));

        CommandException exception =
                assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals(
                "There is no interaction note 2. The history shows 1 note.",
                exception.getMessage());
    }

    @Test
    public void execute_invalidNoteIndexWithTwoNotes_usesPluralNotes()
            throws CommandException {

        // EP: the requested note index is outside a multi-note history.
        // Boundary value: two stored notes require the plural noun in the error.
        InteractionNote firstNote = new InteractionNote(
                "Called client",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        InteractionNote secondNote = new InteractionNote(
                "Discussed renewal",
                LocalDateTime.of(2026, 10, 26, 21, 10));

        Person person = new PersonBuilder()
                .withName("John Tan")
                .withInteractionNotes(firstNote, secondNote)
                .build();

        Model model = new ModelManager();
        model.addPerson(person);

        DeleteNoteCommand command = new DeleteNoteCommand(
                Index.fromOneBased(1),
                Index.fromOneBased(3));

        CommandException exception =
                assertThrows(CommandException.class, () -> command.execute(model));

        assertEquals(
                "There is no interaction note 3. The history shows 2 notes.",
                exception.getMessage());
    }
}
