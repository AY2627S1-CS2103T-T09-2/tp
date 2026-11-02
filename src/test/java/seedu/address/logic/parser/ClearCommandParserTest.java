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
        // EP: null arguments violate the parser precondition.
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsClearCommand() throws ParseException {
        // EP: no confirmation keyword after trimming produces an unconfirmed command.
        assertTrue(parser.parse("") instanceof ClearCommand);
        assertTrue(parser.parse("   ") instanceof ClearCommand);
        assertTrue(parser.parse(" \t\r\n ") instanceof ClearCommand);
    }

    @Test
    public void parse_confirmArgs_returnsClearCommand() throws ParseException {
        // EP: exactly one lowercase confirmation keyword, with optional surrounding whitespace.
        assertTrue(parser.parse("confirm") instanceof ClearCommand);
        assertTrue(parser.parse(" confirm ") instanceof ClearCommand);
    }

    @Test
    public void parse_blankArgs_executeKeepsContacts() throws ParseException {
        // EP: the unconfirmed command must preserve an existing contact list.
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        int originalContactCount = model.getAddressBook().getPersonList().size();

        parser.parse("").execute(model);

        assertEquals(originalContactCount, model.getAddressBook().getPersonList().size());
    }

    @Test
    public void parse_confirmArgs_executeClearsContacts() throws ParseException {
        // EP: the confirmed command must clear an existing contact list.
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        parser.parse("confirm").execute(model);

        assertTrue(model.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        String expectedMessage =
                "The clear command does not accept additional parameters.";

        // EP: unrecognized confirmation text, including numeric input.
        assertParseFailure(parser, "3", expectedMessage);
        assertParseFailure(parser, "extra", expectedMessage);

        // EP: the confirmation keyword followed by an extra argument.
        assertParseFailure(parser, "confirm extra", expectedMessage);

        // EP: a differently cased confirmation keyword; only the exact lowercase keyword is accepted.
        assertParseFailure(parser, "CONFIRM", expectedMessage);

        // EP: unrecognized text remains invalid after trimming.
        assertParseFailure(parser, "  extra  ", expectedMessage);
    }
}
