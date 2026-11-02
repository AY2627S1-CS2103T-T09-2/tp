package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_NO_PARAMETERS_EXPECTED;

import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code ClearCommand}.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses the given arguments and returns a {@code ClearCommand} for execution.
     *
     * @param args Command arguments, which must be blank or exactly "confirm".
     * @return A command that requests or confirms clearing all contacts.
     * @throws ParseException If the arguments are neither blank nor "confirm".
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public ClearCommand parse(String args) throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.strip();

        if (trimmedArgs.isEmpty()) {
            return new ClearCommand(false);
        }

        if (trimmedArgs.equals(ClearCommand.CONFIRM_KEYWORD)) {
            return new ClearCommand(true);
        }

        // @@author kaseyho
        throw new ParseException(
                String.format(MESSAGE_NO_PARAMETERS_EXPECTED, ClearCommand.COMMAND_WORD));
        // @@author
    }
}
