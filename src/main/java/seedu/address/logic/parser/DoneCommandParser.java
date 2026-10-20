package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DoneCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DoneCommand}.
 */
public class DoneCommandParser implements Parser<DoneCommand> {

    /**
     * Parses the given arguments and returns a {@code DoneCommand} for execution.
     *
     * @param args Arguments containing a positive displayed index.
     * @return A command that selects the contact at the given index.
     * @throws ParseException If the arguments do not contain a valid index.
     * @throws NullPointerException If {@code args} is null.
     */
    @Override
    public DoneCommand parse(String args) throws ParseException {
        requireNonNull(args);
        try {
            Index index = ParserUtil.parseIndex(args);
            return new DoneCommand(index);
        } catch (ParseException exception) {
            throw exception;
        }
    }
}
