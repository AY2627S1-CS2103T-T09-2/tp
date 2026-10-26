package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class HistoryCommandTest {

    @Test
    public void execute_notesExist_showsNewestFirstWithTimestamps()
            throws CommandException {

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

        HistoryCommand command =
                new HistoryCommand(Index.fromOneBased(1));

        CommandResult result = command.execute(model);

        String expected = String.join(
                System.lineSeparator(),
                "Interaction history for John Tan:",
                "1. [26-10-2026 21:10] Client requested a quotation",
                "2. [26-10-2026 20:30] Discussed hospital coverage");

        assertEquals(expected, result.getFeedbackToUser());
    }

    @Test
    public void execute_noNotes_showsNoNotesMessage()
            throws CommandException {

        Person person = new PersonBuilder()
                .withName("John Tan")
                .build();

        Model model = new ModelManager();
        model.addPerson(person);

        HistoryCommand command =
                new HistoryCommand(Index.fromOneBased(1));

        CommandResult result = command.execute(model);

        assertEquals(
                "No interaction notes recorded for John Tan.",
                result.getFeedbackToUser());
    }
}
