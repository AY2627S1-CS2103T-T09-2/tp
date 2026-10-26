package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code HistoryCommand}.
 */
public class HistoryCommandParser implements Parser<HistoryCommand> {

    /**
     * Parses the given arguments and returns a {@code HistoryCommand}.
     *
     * @param args Command arguments containing the displayed contact index.
     * @return A command that shows a contact's interaction history.
     * @throws ParseException If the input does not conform to the expected format.
     */
    @Override
    public HistoryCommand parse(String args) throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.trim();

        if (trimmedArgs.isEmpty() || trimmedArgs.matches(".*\\s+.*")) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            HistoryCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(trimmedArgs);

        return new HistoryCommand(index);
    }
}
