package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ContactedCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LastContact;

public class ContactedCommandParserTest {

    private final ContactedCommandParser parser =
            new ContactedCommandParser();

    @Test
    public void parse_validArgs_returnsContactedCommand()
            throws ParseException {

        ContactedCommand expectedCommand =
                new ContactedCommand(
                        Index.fromOneBased(1),
                        new LastContact("27-10-2026"));

        assertEquals(
                expectedCommand,
                parser.parse("1 d/27-10-2026"));
    }

    @Test
    public void parse_missingIndex_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parse("d/27-10-2026"));
    }

    @Test
    public void parse_missingDate_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parse("1"));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parse("0 d/27-10-2026"));
    }

    @Test
    public void parse_invalidDate_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parse("1 d/31-02-2026"));
    }

    @Test
    public void parse_wrongDateFormat_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parse("1 d/2026-10-27"));
    }

    @Test
    public void parse_extraArguments_throwsParseException() {
        assertThrows(ParseException.class, () ->
                parser.parse("1 d/27-10-2026 extra"));
    }
}
