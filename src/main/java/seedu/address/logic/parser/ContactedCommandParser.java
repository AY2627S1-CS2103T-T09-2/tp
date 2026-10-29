package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LAST_CONTACT;

import java.time.LocalDate;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ContactedCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.LastContact;

/**
 * Parses input arguments and creates a new {@code ContactedCommand}.
 */
public class ContactedCommandParser implements Parser<ContactedCommand> {

    @Override
    public ContactedCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_LAST_CONTACT);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_LAST_CONTACT);

        String indexText = argMultimap.getPreamble().trim();

        if (indexText.isEmpty()
                || indexText.matches(".*\\s+.*")
                || argMultimap.getValue(PREFIX_LAST_CONTACT).isEmpty()) {
            throw new ParseException(
                    String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT,
                            ContactedCommand.MESSAGE_USAGE));
        }

        String dateText =
                argMultimap.getValue(PREFIX_LAST_CONTACT)
                        .orElse("")
                        .trim();

        if (dateText.isEmpty()) {
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

        LastContact lastContact = new LastContact(dateText);

        if (lastContact.isAfter(LocalDate.now())) {
            throw new ParseException(
                    LastContact.MESSAGE_FUTURE_DATE);
        }

        return new ContactedCommand(
                index,
                lastContact);
    }
}
