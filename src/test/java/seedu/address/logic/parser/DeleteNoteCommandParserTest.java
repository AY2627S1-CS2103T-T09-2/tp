package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteNoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class DeleteNoteCommandParserTest {

    private final DeleteNoteCommandParser parser =
            new DeleteNoteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteNoteCommand()
            throws ParseException {

        DeleteNoteCommand expectedCommand =
                new DeleteNoteCommand(
                        Index.fromOneBased(1),
                        Index.fromOneBased(2));

        assertEquals(
                expectedCommand,
                parser.parse("1 2"));
    }

    @Test
    public void parse_missingNoteIndex_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("1"));
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("1 2 3"));
    }

    @Test
    public void parse_invalidContactIndex_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("0 1"));
    }

    @Test
    public void parse_invalidNoteIndex_throwsParseException() {
        assertThrows(ParseException.class,
                () -> parser.parse("1 0"));
    }
}
