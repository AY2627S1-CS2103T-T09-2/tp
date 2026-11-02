package seedu.address.logic.parser;

import static seedu.address.logic.commands.FindCommand.MESSAGE_EMPTY_KEYWORD;
import static seedu.address.logic.commands.FindCommand.MESSAGE_EMPTY_PHONE_KEYWORD;
import static seedu.address.logic.commands.FindCommand.MESSAGE_INVALID_PHONE_KEYWORD;
import static seedu.address.logic.commands.FindCommand.MESSAGE_SEARCHES_ONLY_NAMES_AND_PHONES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.List;
import java.util.Optional;

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

        // Another command's prefix, such as st/ or t/, would otherwise be searched as part of a name.
        Optional<Prefix> unsupportedPrefix = ParserUtil.findUnsupportedPrefix(args, PREFIX_PHONE);
        if (unsupportedPrefix.isPresent()) {
            throw new ParseException(ParserUtil.getUnsupportedPrefixMessage(unsupportedPrefix.get(),
                    FindCommand.COMMAND_WORD, MESSAGE_SEARCHES_ONLY_NAMES_AND_PHONES));
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args, PREFIX_PHONE);
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_PHONE);
            if (!argMultimap.getPreamble().isEmpty()) {
                throw new ParseException(MESSAGE_INVALID_PHONE_KEYWORD);
            }
            String phoneKeyword = argMultimap.getValue(PREFIX_PHONE).get();
            if (phoneKeyword.isEmpty()) {
                throw new ParseException(MESSAGE_EMPTY_PHONE_KEYWORD);
            }
            if (!phoneKeyword.matches("\\d+")) {
                throw new ParseException(MESSAGE_INVALID_PHONE_KEYWORD);
            }
            return new FindCommand(new PhoneContainsKeywordPredicate(phoneKeyword));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
