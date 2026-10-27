package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class HistoryCommandParserTest {

    private final HistoryCommandParser parser =
            new HistoryCommandParser();

    @Test
    public void parse_validIndex_returnsHistoryCommand()
            throws ParseException {
        // EP: a positive index, with or without surrounding whitespace, is accepted.
        HistoryCommand expectedCommand =
                new HistoryCommand(Index.fromOneBased(2));

        assertEquals(
                expectedCommand,
                parser.parse("2"));

        assertEquals(
                expectedCommand,
                parser.parse("   2   "));
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        // EP: no argument is supplied, so the required contact index is missing.
        String expectedMessage =
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        HistoryCommand.MESSAGE_USAGE);

        assertParseFailure(
                parser,
                "",
                expectedMessage);
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        // EP: additional tokens follow the index; only one argument is accepted.
        String expectedMessage =
                String.format(
                        MESSAGE_INVALID_COMMAND_FORMAT,
                        HistoryCommand.MESSAGE_USAGE);

        assertParseFailure(
                parser,
                "1 extra",
                expectedMessage);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        // EP: non-positive integers cannot identify a displayed contact.
        // Boundary value: 0 is immediately below the minimum index of 1.
        assertParseFailure(
                parser,
                "0",
                ParserUtil.MESSAGE_INVALID_INDEX);

        assertParseFailure(
                parser,
                "-1",
                ParserUtil.MESSAGE_INVALID_INDEX);

        // EP: non-numeric text cannot be parsed as an index.
        assertParseFailure(
                parser,
                "abc",
                ParserUtil.MESSAGE_INVALID_INDEX);
    }
}
