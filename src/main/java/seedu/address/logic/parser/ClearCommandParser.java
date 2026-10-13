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
     * @param args Command arguments, which must be blank.
     * @return A command that clears all contacts.
     * @throws ParseException If the arguments contain non-whitespace characters.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public ClearCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException(
                    String.format(MESSAGE_NO_PARAMETERS_EXPECTED, ClearCommand.COMMAND_WORD));
        }

        return new ClearCommand();
    }
}
