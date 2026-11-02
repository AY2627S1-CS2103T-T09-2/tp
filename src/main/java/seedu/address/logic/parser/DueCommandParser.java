package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.DueCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new {@code DueCommand}.
 */
public class DueCommandParser implements Parser<DueCommand> {

    public static final String MESSAGE_INVALID_DAYS =
            "Number of days must be a whole number from 0 to " + DueCommand.MAX_DAYS + ", e.g. due 7.";

    private static final String DAYS_REGEX = "\\d+";

    // Without leading zeros, a number with more digits than MAX_DAYS is out of range, so it is
    // rejected before Integer.parseInt could overflow.
    private static final int MAX_SIGNIFICANT_DIGITS = String.valueOf(DueCommand.MAX_DAYS).length();

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
        // Leading zeros do not change the value, so drop them before the length check.
        // "0" and "000" both become "0".
        String significantDigits = trimmedArgs.replaceFirst("^0+(?=\\d)", "");
        if (significantDigits.length() > MAX_SIGNIFICANT_DIGITS) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        int days = Integer.parseInt(significantDigits);
        if (days > DueCommand.MAX_DAYS) {
            throw new ParseException(MESSAGE_INVALID_DAYS);
        }
        return new DueCommand(days);
    }
}
