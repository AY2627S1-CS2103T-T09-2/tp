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

    @Test
    public void parse_prefixFromOtherCommand_throwsParseException() {
        // EP: the message names the prefix and the command, in the same style as add and edit.
        assertParseFailure(parser, " st/CLIENT", "st/ is not used by find. It only searches names, or phone "
                + "numbers with p/, e.g. find John or find p/9123. To filter by status or tag, use list st/STATUS "
                + "or list t/TAG.");

        // EP: a prefix find does not take, which would otherwise be searched as part of a name.
        assertParseFailure(parser, " n/John", unsupportedPrefixMessage("n/"));
        assertParseFailure(parser, " st/CLIENT", unsupportedPrefixMessage("st/"));
        assertParseFailure(parser, " t/referral", unsupportedPrefixMessage("t/"));
        assertParseFailure(parser, " e/john@example.com", unsupportedPrefixMessage("e/"));
        assertParseFailure(parser, " nt/renewal", unsupportedPrefixMessage("nt/"));

        // EP: the prefix comes after a name keyword or after a phone search; the first one is named.
        assertParseFailure(parser, " John t/referral st/CLIENT", unsupportedPrefixMessage("t/"));
        assertParseFailure(parser, " p/9123 st/CLIENT", unsupportedPrefixMessage("st/"));

        // EP: the prefix follows a tab instead of a space.
        assertParseFailure(parser, " John\tst/CLIENT", unsupportedPrefixMessage("st/"));
    }

    @Test
    public void parse_slashInNameKeyword_searchesNames() {
        // EP: name forms that look like a prefix stay name keywords.
        assertParseSuccess(parser, " Ravi a/l d/o s/o",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Ravi", "a/l", "d/o", "s/o"))));
        assertParseSuccess(parser, " A/P D/O",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("A/P", "D/O"))));
        assertParseSuccess(parser, " d/o-Kumar",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("d/o-Kumar"))));

        // EP: prefix-like text that does not start a word is not a prefix.
        assertParseSuccess(parser, " Tan/t/x",
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Tan/t/x"))));
    }

    private static String unsupportedPrefixMessage(String prefix) {
        return prefix + " is not used by find. " + FindCommand.MESSAGE_SEARCHES_ONLY_NAMES_AND_PHONES;
    }
}
