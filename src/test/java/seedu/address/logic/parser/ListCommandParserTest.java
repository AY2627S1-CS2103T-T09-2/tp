package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Tests {@code ListCommandParser}.
 */
public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsListCommand() throws ParseException {
        assertTrue(parser.parse("") instanceof ListCommand);
        assertTrue(parser.parse("   ") instanceof ListCommand);
        assertTrue(parser.parse(" \t\r\n ") instanceof ListCommand);
    }

    @Test
    public void parse_nonBlankArgs_throwsParseException() {
        String expectedMessage = "The list command does not accept additional parameters.";

        assertParseFailure(parser, "3", expectedMessage);
        assertParseFailure(parser, "extra", expectedMessage);
        assertParseFailure(parser, "  extra  ", expectedMessage);
        assertParseFailure(parser, "3 extra", expectedMessage);
    }
}
