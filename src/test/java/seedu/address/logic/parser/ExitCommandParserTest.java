package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tests {@code ExitCommandParser}.
 */
public class ExitCommandParserTest {

    private final ExitCommandParser parser = new ExitCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        // EP: null arguments violate the parser precondition.
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsExitCommand() throws ParseException {
        // EP: no arguments after trimming; empty input and different whitespace forms are equivalent.
        assertTrue(parser.parse("") instanceof ExitCommand);
        assertTrue(parser.parse("   ") instanceof ExitCommand);
        assertTrue(parser.parse(" \t\r\n ") instanceof ExitCommand);
    }

    @Test
    public void parse_nonBlankArgs_throwsParseException() {
        // EP: any nonblank arguments are forbidden, regardless of their contents or surrounding whitespace.
        String expectedMessage = "The exit command does not accept additional parameters.";

        assertParseFailure(parser, "3", expectedMessage);
        assertParseFailure(parser, "extra", expectedMessage);
        assertParseFailure(parser, "  extra  ", expectedMessage);
    }
}
