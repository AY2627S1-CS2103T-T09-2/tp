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
        // EP: omitted duration (empty or whitespace-only input) uses the default of zero days.
        assertParseSuccess(parser, "", new DueCommand(0));
        assertParseSuccess(parser, "   ", new DueCommand(0));
    }

    @Test
    public void parse_validDays_returnsDueCommand() {
        // EP: explicit whole-number durations in [0, 365].
        assertParseSuccess(parser, " 0", new DueCommand(0)); // Boundary value: minimum duration.
        assertParseSuccess(parser, " 7 ", new DueCommand(7)); // Interior value with surrounding whitespace.
        assertParseSuccess(parser, " 365", new DueCommand(365)); // Boundary value: maximum duration.
    }

    @Test
    public void parse_invalidDays_throwsParseException() {
        String[] invalidArgs = {
            " -1", // EP: negative durations; boundary value immediately below zero.
            " abc", // EP: nonnumeric input.
            " 7 days", // EP: extra text after a duration.
            " 1.5", // EP: fractional durations.
            " 366", // EP: durations above 365; boundary value immediately above the maximum.
            " 99999999999", // EP: digit strings exceeding the parser's nine-digit limit.
            " +7" // EP: explicit positive signs, which are not accepted by the digits-only syntax.
        };
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, MESSAGE_INVALID_DAYS);
        }
    }
}
