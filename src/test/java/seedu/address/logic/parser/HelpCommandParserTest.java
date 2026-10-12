package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tests {@code HelpCommandParser}.
 */
public class HelpCommandParserTest {

    private final HelpCommandParser parser = new HelpCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsHelpCommand() throws ParseException {
        assertTrue(parser.parse("") instanceof HelpCommand);
        assertTrue(parser.parse("   ") instanceof HelpCommand);
        assertTrue(parser.parse(" \t\r\n ") instanceof HelpCommand);
    }

    @Test
    public void parse_nonBlankArgs_throwsParseException() {
        String expectedMessage = "The help command does not accept additional parameters.";

        assertParseFailure(parser, "3", expectedMessage);
        assertParseFailure(parser, "extra", expectedMessage);
        assertParseFailure(parser, "  extra  ", expectedMessage);
        assertParseFailure(parser, "3 extra", expectedMessage);
    }
}
