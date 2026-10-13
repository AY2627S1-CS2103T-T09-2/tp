package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Validates a contact index without completing its follow-up.
 */
public class DoneCommand extends Command {

    public static final String COMMAND_WORD = "done";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " INDEX";
    public static final String MESSAGE_SUCCESS =
            "%1$s selected; completing follow-ups is coming soon.";

    private final Index targetIndex;

    /**
     * Constructs a command that selects the contact at the given displayed index.
     *
     * @param targetIndex Index of the contact in the displayed list.
     * @throws NullPointerException If {@code targetIndex} is null.
     */
    public DoneCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    /**
     * Validates the index and returns a message without changing any contacts.
     *
     * @param model Model containing the displayed contacts.
     * @return A result identifying the selected contact.
     * @throws CommandException If the index is outside the displayed list.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person selectedPerson = lastShownList.get(targetIndex.getZeroBased());
        return new CommandResult(String.format(MESSAGE_SUCCESS, selectedPerson.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DoneCommand otherDoneCommand)) {
            return false;
        }

        return targetIndex.equals(otherDoneCommand.targetIndex);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(targetIndex.getZeroBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
