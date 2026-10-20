package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.DueCommandParser.MESSAGE_INVALID_DAYS;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DueCommand;

public class DueCommandParserTest {

    private final DueCommandParser parser = new DueCommandParser();

    @Test
    public void parse_noArgs_returnsDueToday() {
        assertParseSuccess(parser, "", new DueCommand(0));
        assertParseSuccess(parser, "   ", new DueCommand(0));
    }

    @Test
    public void parse_validDays_returnsDueCommand() {
        assertParseSuccess(parser, " 0", new DueCommand(0));
        assertParseSuccess(parser, " 7 ", new DueCommand(7));
        assertParseSuccess(parser, " 365", new DueCommand(365));
    }

    @Test
    public void parse_invalidDays_throwsParseException() {
        String[] invalidArgs = {" -1", " abc", " 7 days", " 1.5", " 366", " 99999999999", " +7"};
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, MESSAGE_INVALID_DAYS);
        }
    }
}
