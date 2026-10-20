package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DueCommand}.
 */
public class DueCommandParser implements Parser<DueCommand> {

    public static final String MESSAGE_INVALID_DAYS =
            "Number of days must be a whole number from 0 to " + DueCommand.MAX_DAYS + ", e.g. due 7";

    /**
     * Parses the given arguments and returns a {@code DueCommand}.
     * No arguments means follow-ups due today or earlier.
     *
     * @throws ParseException If the number of days is not a whole number from 0 to {@code DueCommand.MAX_DAYS}.
     */
    @Override
    public DueCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return new DueCommand(0);
        }

        // At most 9 digits, so parseInt cannot overflow.
        if (!trimmedArgs.matches("\\d{1,9}")) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        int days = Integer.parseInt(trimmedArgs);
        if (days > DueCommand.MAX_DAYS) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        return new DueCommand(days);
    }
}
