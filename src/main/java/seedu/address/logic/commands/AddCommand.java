package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Person;

/**
 * Adds a contact to Easy-Insurance.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact to Easy-Insurance. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_ADDRESS + "ADDRESS "
            + "[" + PREFIX_STATUS + "STATUS] "
            + "[" + PREFIX_FOLLOW_UP + "FOLLOW_UP_DATE] "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_STATUS + "PROSPECT "
            + PREFIX_FOLLOW_UP + "25-12-2026 "
            + PREFIX_TAG + "friends "
            + PREFIX_TAG + "owesMoney";

    public static final String MESSAGE_SUCCESS = "New contact added: %1$s (%2$s)";
    public static final String MESSAGE_HIDDEN_CONTACT =
            "\nThis contact is hidden by the current view. Type 'list' to show all contacts.";
    public static final String MESSAGE_DISPLAYED_INDEX = "\nShown at index %d.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This contact already exists: %1$s (%2$s).";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            Person existingPerson = model.getAddressBook().getPersonList().stream()
                    .filter(toAdd::isSamePerson)
                    .findFirst()
                    .orElseThrow();
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PERSON,
                    existingPerson.getName(), existingPerson.getPhone()));
        }

        model.addPerson(toAdd);
        String feedback = String.format(MESSAGE_SUCCESS, toAdd.getName(), toAdd.getStatus());
        int displayedIndex = model.getFilteredPersonList().indexOf(toAdd);
        if (displayedIndex < 0) {
            feedback += MESSAGE_HIDDEN_CONTACT;
        } else {
            feedback += String.format(MESSAGE_DISPLAYED_INDEX, displayedIndex + 1);
        }
        feedback += toAdd.getFollowUp().filter(FollowUp::isPast).map(FollowUp::getPastDateWarning).orElse("");
        return displayedIndex < 0 ? new CommandResult(feedback) : new CommandResult(feedback, displayedIndex);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
