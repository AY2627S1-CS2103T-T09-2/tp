package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DoneCommand;

/**
 * Tests {@code DoneCommandParser}.
 */
public class DoneCommandParserTest {

    private final DoneCommandParser parser = new DoneCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_validArgs_returnsDoneCommand() {
        DoneCommand expectedCommand = new DoneCommand(INDEX_FIRST_PERSON);

        assertParseSuccess(parser, "1", expectedCommand);
        assertParseSuccess(parser, " \t1 ", expectedCommand);
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = MESSAGE_INVALID_INDEX;
        String[] invalidArgs = {"0", "-1", "+1", "abc", "2147483648"};

        for (String args : invalidArgs) {
            assertParseFailure(parser, args, expectedMessage);
        }
        String usageMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DoneCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "", usageMessage);
        assertParseFailure(parser, "1 extra", usageMessage);
    }
}
