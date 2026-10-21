package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.FindCommand.MESSAGE_EMPTY_KEYWORD;
import static seedu.address.logic.commands.FindCommand.MESSAGE_EMPTY_PHONE_KEYWORD;
import static seedu.address.logic.commands.FindCommand.MESSAGE_INVALID_PHONE_KEYWORD;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyInput_throwsParseException() {
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse("   "));

        assertEquals(MESSAGE_EMPTY_KEYWORD, exception.getMessage());
    }

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", MESSAGE_EMPTY_KEYWORD);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);
        assertParseSuccess(parser, "p/9123",
                new FindCommand(new PhoneContainsKeywordPredicate("9123")));

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_invalidPhoneKeyword_throwsParseException() {
        assertParseFailure(parser, "p/", MESSAGE_EMPTY_PHONE_KEYWORD);
        assertParseFailure(parser, "p/abc", MESSAGE_INVALID_PHONE_KEYWORD);
        assertParseFailure(parser, "p/9123 alice", MESSAGE_INVALID_PHONE_KEYWORD);
        assertParseFailure(parser, "alice p/9123", MESSAGE_INVALID_PHONE_KEYWORD);
    }

}
