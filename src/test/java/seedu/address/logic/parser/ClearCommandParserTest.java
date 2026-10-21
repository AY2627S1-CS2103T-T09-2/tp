package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

/**
 * Tests {@code ClearCommandParser}.
 */
public class ClearCommandParserTest {

    private final ClearCommandParser parser = new ClearCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsClearCommand() throws ParseException {
        assertTrue(parser.parse("") instanceof ClearCommand);
        assertTrue(parser.parse("   ") instanceof ClearCommand);
        assertTrue(parser.parse(" \t\r\n ") instanceof ClearCommand);
    }

    @Test
    public void parse_confirmArgs_returnsClearCommand() throws ParseException {
        assertTrue(parser.parse("confirm") instanceof ClearCommand);
        assertTrue(parser.parse(" confirm ") instanceof ClearCommand);
    }

    @Test
    public void parse_blankArgs_executeKeepsContacts() throws ParseException {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        int originalContactCount = model.getAddressBook().getPersonList().size();

        parser.parse("").execute(model);

        assertEquals(originalContactCount, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void parse_confirmArgs_executeClearsContacts() throws ParseException {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        parser.parse("confirm").execute(model);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage =
                "The clear command does not accept additional parameters.";

        assertParseFailure(parser, "3", expectedMessage);
        assertParseFailure(parser, "extra", expectedMessage);
        assertParseFailure(parser, "confirm extra", expectedMessage);
        assertParseFailure(parser, "CONFIRM", expectedMessage);
        assertParseFailure(parser, "  extra  ", expectedMessage);
    }
}
