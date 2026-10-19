package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Parses optional status and tag filters for the list command.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses arguments and returns a command that lists matching contacts.
     *
     * @param args Arguments containing at most one status and one tag filter.
     * @return A command that lists all contacts or applies the supplied filters.
     * @throws ParseException If the arguments or filter values are invalid.
     * @throws NullPointerException If the arguments are null.
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new ListCommand();
        }

        String normalizedArgs = " " + args.strip().replaceAll("\\s+", " ");
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_STATUS, PREFIX_TAG);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STATUS, PREFIX_TAG);

        Optional<Status> status = Optional.empty();
        if (argMultimap.getValue(PREFIX_STATUS).isPresent()) {
            status = Optional.of(ParserUtil.parseStatus(argMultimap.getValue(PREFIX_STATUS).get()));
        }

        Optional<Tag> tag = Optional.empty();
        if (argMultimap.getValue(PREFIX_TAG).isPresent()) {
            tag = Optional.of(ParserUtil.parseTag(argMultimap.getValue(PREFIX_TAG).get()));
        }

        return new ListCommand(status, tag);
    }
}
