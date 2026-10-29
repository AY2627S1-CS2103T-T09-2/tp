package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.InteractionNote;

public class NoteCommandParserTest {

    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_validArgs_returnsNoteCommand() throws ParseException {
        NoteCommand expectedCommand = new NoteCommand(
                Index.fromOneBased(1),
                new InteractionNote("Called client"));

        assertEquals(
                expectedCommand,
                parser.parse("1 nt/Called client"));
    }

    @Test
    public void parse_missingSpaceBeforePrefix_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("1nt/nospace"));
    }

    @Test
    public void parse_duplicateNotePrefix_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("1 nt/a nt/b"));
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("nt/Called client"));
    }

    @Test
    public void parse_missingNote_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("1 nt/"));
    }
}
