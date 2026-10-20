package seedu.address.logic.parser;

import static seedu.address.logic.commands.FindCommand.MESSAGE_EMPTY_KEYWORD;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.PhoneContainsKeywordPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_KEYWORD);
        }

        if (trimmedArgs.startsWith("p/")) {
            String phoneKeyword = trimmedArgs.substring(2).trim();
            if (phoneKeyword.isEmpty() || phoneKeyword.contains(" ")) {
                throw new ParseException(MESSAGE_EMPTY_KEYWORD);
            }
            return new FindCommand(new PhoneContainsKeywordPredicate(phoneKeyword));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
