package seedu.address.logic;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command. Type help to see all commands.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The contact index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d contacts listed.";
    public static final String MESSAGE_PERSON_LISTED_OVERVIEW = "1 contact listed.";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_NO_PARAMETERS_EXPECTED =
            "The %1$s command does not accept additional parameters.";

    /**
     * Returns a grammatically correct result-count message.
     */
    public static String getPersonListOverview(int numberOfContacts) {
        if (numberOfContacts == 1) {
            return MESSAGE_PERSON_LISTED_OVERVIEW;
        }

        return String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, numberOfContacts);
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Status: ")
                .append(person.getStatus())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        person.getFollowUp().ifPresent(followUp -> builder.append("; Follow-up: ").append(followUp));
        return builder.toString();
    }

}
