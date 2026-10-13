package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_NO_PARAMETERS_EXPECTED;

import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ListCommand}.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses the given arguments and returns a {@code ListCommand} for execution.
     *
     * @param args Command arguments, which must be blank.
     * @return A command that lists all contacts.
     * @throws ParseException If the arguments contain non-whitespace characters.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(
                    String.format(MESSAGE_NO_PARAMETERS_EXPECTED, ListCommand.COMMAND_WORD));
        }

        return new ListCommand();
    }
}
