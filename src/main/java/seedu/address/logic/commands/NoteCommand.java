package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;

/**
 * Adds an interaction note to an existing contact.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds an interaction note to the contact identified by the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_NOTE + "NOTE\n"
            + "Example: " + COMMAND_WORD + " 2 "
            + PREFIX_NOTE + "Discussed hospital coverage with client";

    public static final String MESSAGE_SUCCESS =
            "Added interaction note for %1$s.";

    private final Index targetIndex;
    private final InteractionNote interactionNote;

    /**
     * Creates a {@code NoteCommand}.
     *
     * @param targetIndex Index of the contact in the displayed list.
     * @param interactionNote Note to add to the contact.
     */
    public NoteCommand(Index targetIndex, InteractionNote interactionNote) {
        this.targetIndex = requireNonNull(targetIndex);
        this.interactionNote = requireNonNull(interactionNote);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(
                    Messages.getInvalidPersonDisplayedIndexMessage(
                            targetIndex.getOneBased(), lastShownList.size()));
        }

        Person personToUpdate = lastShownList.get(targetIndex.getZeroBased());

        List<InteractionNote> updatedNotes =
                new ArrayList<>(personToUpdate.getInteractionNotes());
        updatedNotes.add(interactionNote);

        Person updatedPerson = new Person(
                personToUpdate.getName(),
                personToUpdate.getPhone(),
                personToUpdate.getEmail(),
                personToUpdate.getAddress(),
                personToUpdate.getStatus(),
                personToUpdate.getTags(),
                personToUpdate.getFollowUp(),
                updatedNotes);

        model.setPerson(personToUpdate, updatedPerson);

        return new CommandResult(
                String.format(MESSAGE_SUCCESS, personToUpdate.getName()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof NoteCommand otherNoteCommand)) {
            return false;
        }

        return targetIndex.equals(otherNoteCommand.targetIndex)
                && interactionNote.equals(otherNoteCommand.interactionNote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, interactionNote);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("interactionNote", interactionNote)
                .toString();
    }
}
