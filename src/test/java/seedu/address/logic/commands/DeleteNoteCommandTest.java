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
    public void execute_deleteDisplayedNote_preservesContactAndOtherNotes()
            throws CommandException {

        // EP: the selected contact has multiple notes, so deleting one must preserve the others.
        // Boundary value: contact and note indexes are both 1, the first valid displayed indexes.
        // The newest note is stored second, so deletion must use history order rather than storage order.
        InteractionNote olderNote = new InteractionNote(
                "Discussed hospital coverage",
                LocalDateTime.of(2026, 10, 26, 20, 30));

        InteractionNote newerNote = new InteractionNote(
                "Client requested a quotation",
                LocalDateTime.of(2026, 10, 26, 21, 10));

        Person person = new PersonBuilder()
                .withName("John Tan")
                .withInteractionNotes(olderNote, newerNote)
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
                "Deleted interaction note 1 for John Tan: Client requested a quotation",
                result.getFeedbackToUser());

        assertEquals(person.getName(), updatedPerson.getName());
        assertEquals(person.getPhone(), updatedPerson.getPhone());
        assertEquals(person.getEmail(), updatedPerson.getEmail());
        assertEquals(person.getAddress(), updatedPerson.getAddress());
        assertEquals(person.getStatus(), updatedPerson.getStatus());
        assertEquals(person.getTags(), updatedPerson.getTags());
        assertEquals(person.getFollowUp(), updatedPerson.getFollowUp());

        assertEquals(
                List.of(olderNote),
                updatedPerson.getInteractionNotes());
    }

    @Test
    public void execute_invalidNoteIndexWithOneNote_usesSingularNote()
            throws CommandException {

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
