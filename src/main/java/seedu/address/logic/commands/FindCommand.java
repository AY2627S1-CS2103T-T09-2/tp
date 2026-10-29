package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Finds and lists contacts by name-prefix or phone-number search.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds contacts by name prefix or phone digits and "
            + "displays them as a list with index numbers.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]... or p/PHONE_DIGITS\n"
            + "Example: " + COMMAND_WORD + " alice bob charlie";

    public static final String MESSAGE_EMPTY_KEYWORD =
            "Please enter at least one name keyword to search for. Example: find alice tan";
    public static final String MESSAGE_EMPTY_PHONE_KEYWORD =
            "Please enter at least one phone digit to search for. Example: find p/9123";
    public static final String MESSAGE_INVALID_PHONE_KEYWORD =
            "Phone search keywords must contain digits only. Example: find p/9123";
    /** Follows the "... is not used by find." error for a prefix that find does not take. */
    public static final String MESSAGE_SEARCHES_ONLY_NAMES_AND_PHONES = "It only searches names, "
            + "or phone numbers with p/, e.g. find John or find p/9123. "
            + "To filter by status or tag, use list st/STATUS or list t/TAG.";

    private final Predicate<Person> predicate;

    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                Messages.getPersonListOverview(model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
