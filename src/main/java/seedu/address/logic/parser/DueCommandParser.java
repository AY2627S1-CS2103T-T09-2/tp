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

    // At most 9 digits, so Integer.parseInt cannot overflow before the range check.
    private static final String DAYS_REGEX = "\\d{1,9}";

    /**
     * Parses the given arguments and returns a {@code DueCommand}.
     * No arguments means follow-ups due today or earlier.
     *
     * @throws ParseException if the number of days is not a whole number from 0 to {@code DueCommand.MAX_DAYS}.
     */
    @Override
    public DueCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            return new DueCommand(0);
        }

        if (!trimmedArgs.matches(DAYS_REGEX)) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        int days = Integer.parseInt(trimmedArgs);
        if (days > DueCommand.MAX_DAYS) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        return new DueCommand(days);
    }
}
