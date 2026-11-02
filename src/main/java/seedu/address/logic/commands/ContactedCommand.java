package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.LastContact;
import seedu.address.model.person.Person;

/**
 * Updates the last contact date of an existing contact.
 */
public class ContactedCommand extends Command {

    public static final String COMMAND_WORD = "contacted";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Updates the last contact date of the contact identified by the displayed index.\n"
            + "Parameters: INDEX d/DD-MM-YYYY\n"
            + "Example: " + COMMAND_WORD + " 2 d/27-10-2026";

    public static final String MESSAGE_SUCCESS =
            "Updated last contact date for %1$s to %2$s.";

    private final Index targetIndex;
    private final LastContact lastContact;

    /**
     * Creates a {@code ContactedCommand}.
     *
     * @param targetIndex Index of the contact in the displayed list.
     * @param lastContact Last contact date to set.
     */
    public ContactedCommand(Index targetIndex, LastContact lastContact) {
        this.targetIndex = requireNonNull(targetIndex);
        this.lastContact = requireNonNull(lastContact);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(
                    Messages.getInvalidPersonDisplayedIndexMessage(
                            targetIndex.getOneBased(),
                            lastShownList.size()));
        }

        Person personToUpdate =
                lastShownList.get(targetIndex.getZeroBased());

        Person updatedPerson = new Person(
                personToUpdate.getName(),
                personToUpdate.getPhone(),
                personToUpdate.getEmail(),
                personToUpdate.getAddress(),
                personToUpdate.getStatus(),
                personToUpdate.getTags(),
                personToUpdate.getFollowUp(),
                Optional.of(lastContact),
                personToUpdate.getInteractionNotes());

        model.setPerson(personToUpdate, updatedPerson);

        return new CommandResult(
                String.format(
                        MESSAGE_SUCCESS,
                        personToUpdate.getName(),
                        lastContact));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof ContactedCommand otherCommand
                && targetIndex.equals(otherCommand.targetIndex)
                && lastContact.equals(otherCommand.lastContact));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, lastContact);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("lastContact", lastContact)
                .toString();
    }
}
