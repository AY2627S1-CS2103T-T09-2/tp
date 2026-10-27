package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ContactedCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LastContact;

/**
 * Parses input arguments and creates a new {@code ContactedCommand}.
 */
public class ContactedCommandParser implements Parser<ContactedCommand> {

    private static final String DATE_PREFIX = "d/";

    @Override
    public ContactedCommand parse(String args) throws ParseException {
        requireNonNull(args);

        String trimmedArgs = args.trim();

        int prefixPosition = trimmedArgs.indexOf(DATE_PREFIX);

        if (prefixPosition < 0) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            ContactedCommand.MESSAGE_USAGE));
        }

        String indexText =
                trimmedArgs.substring(0, prefixPosition).trim();

        String dateText =
                trimmedArgs.substring(
                        prefixPosition + DATE_PREFIX.length()).trim();

        if (indexText.isEmpty()
                || indexText.matches(".*\\s+.*")
                || dateText.isEmpty()
                || dateText.matches(".*\\s+.*")) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            ContactedCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(indexText);

        if (!LastContact.isValidLastContact(dateText)) {
            throw new ParseException(
                    LastContact.MESSAGE_CONSTRAINTS);
        }

        return new ContactedCommand(
                index,
                new LastContact(dateText));
    }
}
