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

        // EP: exactly two positive indexes identify the contact and its note.
        // Boundary value: 1 is the smallest valid contact index.
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
        // EP: fewer than two arguments; the required note index is missing.
        assertThrows(ParseException.class, () ->
                parser.parse("1"));
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        // EP: more than two arguments; extra tokens are not accepted.
        assertThrows(ParseException.class, () ->
                parser.parse("1 2 3"));
    }

    @Test
    public void parse_invalidContactIndex_throwsParseException() {
        // EP: a non-positive contact index with a valid note index.
        // Boundary value: 0 is immediately below the minimum index of 1.
        assertThrows(ParseException.class, () ->
                parser.parse("0 1"));
    }

    @Test
    public void parse_invalidNoteIndex_throwsParseException() {
        // EP: a non-positive note index with a valid contact index.
        // Boundary value: 0 is immediately below the minimum index of 1.
        assertThrows(ParseException.class, () ->
                parser.parse("1 0"));
    }
}
