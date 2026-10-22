package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Completes the pending follow-up of a contact identified by its displayed index.
 */
public class DoneCommand extends Command {

    public static final String COMMAND_WORD = "done";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " INDEX";
    public static final String MESSAGE_SUCCESS = "Marked follow-up for %1$s as completed.";
    public static final String MESSAGE_NO_PENDING_FOLLOW_UP = "%1$s does not have a pending follow-up.";

    private static final Logger logger = LogsCenter.getLogger(DoneCommand.class);

    private final Index targetIndex;

    /**
     * Constructs a command that completes the follow-up for the given displayed index.
     *
     * @param targetIndex Index of the contact in the displayed list.
     * @throws NullPointerException If {@code targetIndex} is null.
     */
    public DoneCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
    }

    /**
     * Clears the selected contact's pending follow-up and returns a confirmation message.
     *
     * @param model Model containing the displayed contacts.
     * @return A result confirming that the follow-up was completed.
     * @throws CommandException If the index is outside the displayed list or the contact has
     *     no pending follow-up.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            logger.fine(() -> "Rejected done; displayedIndex=" + targetIndex.getOneBased()
                    + ", displayedCount=" + lastShownList.size());
            throw new CommandException(Messages.getInvalidPersonDisplayedIndexMessage(
                    targetIndex.getOneBased(), lastShownList.size()));
        }

        Person personToComplete = lastShownList.get(targetIndex.getZeroBased());
        if (personToComplete.getFollowUp().isEmpty()) {
            logger.fine(() -> "Rejected done; no pending follow-up, displayedIndex=" + targetIndex.getOneBased());
            throw new CommandException(
                    String.format(MESSAGE_NO_PENDING_FOLLOW_UP, personToComplete.getName()));
        }

        Person completedPerson = new Person(
                personToComplete.getName(), personToComplete.getPhone(), personToComplete.getEmail(),
                personToComplete.getAddress(), personToComplete.getStatus(), personToComplete.getTags(),
                Optional.empty());
        model.setPerson(personToComplete, completedPerson);
        logger.info(() -> "Cleared pending follow-up in model; displayedIndex=" + targetIndex.getOneBased());

        return new CommandResult(String.format(MESSAGE_SUCCESS, personToComplete.getName()));
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
