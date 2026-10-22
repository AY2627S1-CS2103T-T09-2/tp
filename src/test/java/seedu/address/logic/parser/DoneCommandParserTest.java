package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DoneCommand;
import seedu.address.testutil.LogCapture;

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

        try (LogCapture logs = new LogCapture(DoneCommandParser.class)) {
            assertParseSuccess(parser, "1", expectedCommand);
            assertParseSuccess(parser, " \t1 ", expectedCommand);
            assertTrue(logs.getRecords().isEmpty());
        }
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage = MESSAGE_INVALID_INDEX;
        String[] invalidArgs = {"0", "-1", "+1", "abc", "2147483648"};

        for (String args : invalidArgs) {
            assertLoggedParseFailure(args, expectedMessage, "invalid index");
        }
        String usageMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DoneCommand.MESSAGE_USAGE);
        assertLoggedParseFailure("", usageMessage, "expected a single index");
        assertLoggedParseFailure("1 extra", usageMessage, "expected a single index");
    }

    @Test
    public void parse_contactDetailsInArgs_doesNotLogRawInput() {
        String args = "1 johntan@example.com";
        try (LogCapture logs = new LogCapture(DoneCommandParser.class)) {
            assertParseFailure(parser, args, String.format(MESSAGE_INVALID_COMMAND_FORMAT, DoneCommand.MESSAGE_USAGE));
            logs.assertSingleLog(Level.FINE, "expected a single index");
            assertFalse(logs.getRecords().getFirst().getMessage().contains("johntan@example.com"));
        }
    }

    private void assertLoggedParseFailure(String args, String expectedMessage, String reason) {
        try (LogCapture logs = new LogCapture(DoneCommandParser.class)) {
            assertParseFailure(parser, args, expectedMessage);
            logs.assertSingleLog(Level.FINE, reason);
        }
    }
}
