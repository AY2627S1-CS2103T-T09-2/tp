package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_NO_PARAMETERS_EXPECTED;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ExitCommand}.
 */
public class ExitCommandParser implements Parser<ExitCommand> {

    /**
     * Parses the given arguments and returns an {@code ExitCommand} for execution.
     *
     * @param args Command arguments, which must be blank.
     * @return A command that exits the application.
     * @throws ParseException If the arguments contain non-whitespace characters.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public ExitCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(
                    String.format(MESSAGE_NO_PARAMETERS_EXPECTED, ExitCommand.COMMAND_WORD));
        }

        return new ExitCommand();
    }
}
