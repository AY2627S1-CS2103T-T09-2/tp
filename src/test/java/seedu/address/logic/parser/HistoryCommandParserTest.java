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
        assertParseFailure(
                parser,
                "0",
                ParserUtil.MESSAGE_INVALID_INDEX);

        assertParseFailure(
                parser,
                "-1",
                ParserUtil.MESSAGE_INVALID_INDEX);

        assertParseFailure(
                parser,
                "abc",
                ParserUtil.MESSAGE_INVALID_INDEX);
    }
}
