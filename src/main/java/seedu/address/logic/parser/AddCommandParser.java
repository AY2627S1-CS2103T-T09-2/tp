package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    public static final String MESSAGE_BLANK_FOLLOW_UP =
            "Follow-up date must not be blank when adding a contact. Leave out f/ if there is no follow-up yet.";

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_STATUS, PREFIX_TAG, PREFIX_FOLLOW_UP);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_ADDRESS, PREFIX_PHONE, PREFIX_EMAIL)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_STATUS, PREFIX_FOLLOW_UP);
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        Status status = argMultimap.getValue(PREFIX_STATUS).isPresent()
                ? ParserUtil.parseStatus(argMultimap.getValue(PREFIX_STATUS).get()) : Status.DEFAULT;
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
        Optional<FollowUp> followUp = parseOptionalFollowUp(argMultimap);

        Person person = new Person(name, phone, email, address, status, tagList, followUp);

        return new AddCommand(person);
    }

    /**
     * Parses the optional follow-up date in {@code argMultimap}.
     * Unlike edit, where an empty {@code f/} clears the date, an empty {@code f/} on add is rejected
     * so that the user is told to leave the prefix out instead of seeing a date format error.
     *
     * @throws ParseException if {@code f/} is given with a blank or invalid date.
     */
    private static Optional<FollowUp> parseOptionalFollowUp(ArgumentMultimap argMultimap) throws ParseException {
        Optional<String> followUpValue = argMultimap.getValue(PREFIX_FOLLOW_UP);
        if (followUpValue.isEmpty()) {
            return Optional.empty();
        }
        if (followUpValue.get().isBlank()) {
            throw new ParseException(MESSAGE_BLANK_FOLLOW_UP);
        }
        return Optional.of(ParserUtil.parseFollowUp(followUpValue.get()));
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
