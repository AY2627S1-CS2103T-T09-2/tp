package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_REQUIRED_FIELDS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
        ParserUtil.verifyNoUnsupportedPrefix(args, AddCommand.COMMAND_WORD, PREFIX_NAME, PREFIX_PHONE,
                PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_STATUS, PREFIX_TAG, PREFIX_FOLLOW_UP);

        // Text before the first prefix is most likely a name typed without n/. In that case the missing-field
        // message below says that n/NAME is missing. If n/ is given, the text cannot be explained that way.
        if (!argMultimap.getPreamble().isEmpty() && argMultimap.getValue(PREFIX_NAME).isPresent()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        String missingFields = findMissingRequiredFields(argMultimap);
        if (!missingFields.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_MISSING_REQUIRED_FIELDS, missingFields, AddCommand.MESSAGE_USAGE));
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
     * Returns the required fields that are absent from {@code argMultimap}, such as {@code "e/EMAIL, a/ADDRESS"},
     * listed in the same order as the add command format. Returns an empty string if none are missing.
     */
    private static String findMissingRequiredFields(ArgumentMultimap argMultimap) {
        return Stream.of(RequiredField.values())
                .filter(field -> argMultimap.getValue(field.prefix).isEmpty())
                .map(RequiredField::toString)
                .collect(Collectors.joining(", "));
    }

    /**
     * Required fields of the add command, declared in the order they appear in the command format.
     */
    private enum RequiredField {
        NAME(PREFIX_NAME),
        PHONE(PREFIX_PHONE),
        EMAIL(PREFIX_EMAIL),
        ADDRESS(PREFIX_ADDRESS);

        private final Prefix prefix;

        RequiredField(Prefix prefix) {
            this.prefix = prefix;
        }

        @Override
        public String toString() {
            return prefix + name();
        }
    }

}
