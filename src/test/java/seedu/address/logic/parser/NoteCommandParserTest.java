package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_noteContainsNotePrefix_success() throws ParseException {
        String noteText = "Client asked what nt/ means";

        NoteCommand expectedCommand = new NoteCommand(
                Index.fromOneBased(1),
                new InteractionNote(noteText));

        assertEquals(
                expectedCommand,
                parser.parse("1 nt/" + noteText));
    }
}
