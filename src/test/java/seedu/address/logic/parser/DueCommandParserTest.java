package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.DueCommandParser.MESSAGE_INVALID_DAYS;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class DueCommandParserTest {

    private final DueCommandParser parser = new DueCommandParser();

    /**
     * Returns the command the parser is expected to build: {@code days} days from today in the user's time zone.
     */
    private static DueCommand dueIn(int days) {
        return new DueCommand(days, Clock.systemDefaultZone());
    }

    @Test
    public void parse_noArgs_returnsDueToday() {
        // EP: omitted duration (empty or whitespace-only input) uses the default of zero days.
        assertParseSuccess(parser, "", dueIn(0));
        assertParseSuccess(parser, "   ", dueIn(0));
    }

    @Test
    public void parse_validDays_returnsDueCommand() {
        // EP: explicit whole-number durations in [0, 365].
        assertParseSuccess(parser, " 0", dueIn(0)); // Boundary value: minimum duration.
        assertParseSuccess(parser, " 7 ", dueIn(7)); // Interior value with surrounding whitespace.
        assertParseSuccess(parser, " 365", dueIn(365)); // Boundary value: maximum duration.
    }

    @Test
    public void parse_leadingZeros_ignored() {
        // EP: leading zeros do not change the value, however many there are.
        assertParseSuccess(parser, " 000", dueIn(0)); // Only zeros still means zero days.
        assertParseSuccess(parser, " 007", dueIn(7));
        assertParseSuccess(parser, " 0000000365", dueIn(365)); // More than nine characters.
        assertParseSuccess(parser, " 00000000000000000000000007", dueIn(7));
        assertParseFailure(parser, " 0000000366", MESSAGE_INVALID_DAYS); // Still above the maximum.
    }

    @Test
    public void parse_invalidDays_throwsParseException() {
        String[] invalidArgs = {
            " -1", // EP: negative durations; boundary value immediately below zero.
            " abc", // EP: nonnumeric input.
            " 7 days", // EP: extra text after a duration.
            " 1.5", // EP: fractional durations.
            " 366", // EP: durations above 365; boundary value immediately above the maximum.
            " 1000", // EP: more significant digits than the maximum has.
            " 99999999999", // EP: values too large for an int, rejected without overflowing.
            " +7" // EP: explicit positive signs, which are not accepted by the digits-only syntax.
        };
        for (String args : invalidArgs) {
            assertParseFailure(parser, args, MESSAGE_INVALID_DAYS);
        }
    }

    @Test
    public void parse_validDays_usesSystemDefaultZone() throws ParseException {
        // "Today" must be the user's local date. Pacific/Kiritimati is UTC+14, so a command using UTC
        // or any other fixed zone would not be equal, whatever time zone the tests run in.
        ZoneId kiritimati = ZoneId.of("Pacific/Kiritimati");
        TimeZone originalTimeZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(kiritimati));
            DueCommand command = parser.parse(" 7");

            assertEquals(new DueCommand(7, Clock.system(kiritimati)), command);
            assertNotEquals(new DueCommand(7, Clock.systemUTC()), command);
            assertEquals(new DueCommand(0, Clock.system(kiritimati)), parser.parse(""));
        } finally {
            TimeZone.setDefault(originalTimeZone);
        }
    }
}
