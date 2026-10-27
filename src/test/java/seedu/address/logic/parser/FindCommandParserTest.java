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
        // EP: whitespace-only input has no search keyword after trimming.
        ParseException exception = assertThrows(ParseException.class, () -> parser.parse("   "));

        assertEquals(MESSAGE_EMPTY_KEYWORD, exception.getMessage());
    }

    @Test
    public void parse_emptyArg_throwsParseException() {
        // EP: whitespace-only input; adding spaces still cannot supply a keyword.
        assertParseFailure(parser, "     ", MESSAGE_EMPTY_KEYWORD);
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // EP: name search with one or more whitespace-separated keywords.
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // EP: phone search with a nonempty digits-only keyword.
        assertParseSuccess(parser, "p/9123",
                new FindCommand(new PhoneContainsKeywordPredicate("9123")));

        // EP: name keywords separated or surrounded by additional whitespace.
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_invalidPhoneKeyword_throwsParseException() {
        // EP: empty phone keyword; zero digits is the boundary below the required one or more digits.
        assertParseFailure(parser, "p/", MESSAGE_EMPTY_PHONE_KEYWORD);

        // EP: phone keywords containing nondigits.
        assertParseFailure(parser, "p/abc", MESSAGE_INVALID_PHONE_KEYWORD);
        assertParseFailure(parser, "p/9123 alice", MESSAGE_INVALID_PHONE_KEYWORD);

        // EP: phone search with an unsupported name-search preamble.
        assertParseFailure(parser, "alice p/9123", MESSAGE_INVALID_PHONE_KEYWORD);
    }

}
