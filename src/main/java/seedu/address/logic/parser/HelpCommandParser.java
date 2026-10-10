package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code HelpCommand}.
 */
public class HelpCommandParser implements Parser<HelpCommand> {

    /**
     * Parses the given arguments and returns a {@code HelpCommand} for execution.
     *
     * @param args Command arguments, which must be blank.
     * @return A command that shows the help window.
     * @throws ParseException If the arguments contain non-whitespace characters.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public HelpCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (!args.isBlank()) {
            throw new ParseException("The help command does not accept additional parameters.");
        }

        return new HelpCommand();
    }
}
