package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
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
 * Deletes an interaction note from a contact using its displayed history index.
 */
public class DeleteNoteCommand extends Command {

    public static final String COMMAND_WORD = "deletenote";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes an interaction note from the contact identified by the displayed contact index.\n"
            + "Parameters: CONTACT_INDEX NOTE_INDEX "
            + "(both must be positive integers)\n"
            + "Example: " + COMMAND_WORD + " 1 2";

    public static final String MESSAGE_SUCCESS =
            "Deleted interaction note %2$d for %1$s.";

    public static final String MESSAGE_INVALID_NOTE_INDEX =
            "There is no interaction note %1$d. The history shows %2$d notes.";

    private final Index contactIndex;
    private final Index noteIndex;

    /**
     * Creates a {@code DeleteNoteCommand}.
     */
    public DeleteNoteCommand(Index contactIndex, Index noteIndex) {
        this.contactIndex = requireNonNull(contactIndex);
        this.noteIndex = requireNonNull(noteIndex);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        List<Person> lastShownList = model.getFilteredPersonList();

        if (contactIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(
                    Messages.getInvalidPersonDisplayedIndexMessage(
                            contactIndex.getOneBased(),
                            lastShownList.size()));
        }

        Person personToUpdate =
                lastShownList.get(contactIndex.getZeroBased());

        List<InteractionNote> displayedNotes =
                personToUpdate.getInteractionNotes().stream()
                        .sorted(Comparator.comparing(
                                InteractionNote::getCreatedAt).reversed())
                        .toList();

        if (noteIndex.getZeroBased() >= displayedNotes.size()) {
            throw new CommandException(
                    String.format(
                            MESSAGE_INVALID_NOTE_INDEX,
                            noteIndex.getOneBased(),
                            displayedNotes.size()));
        }

        InteractionNote noteToDelete =
                displayedNotes.get(noteIndex.getZeroBased());

        List<InteractionNote> updatedNotes =
                new ArrayList<>(personToUpdate.getInteractionNotes());

        updatedNotes.remove(noteToDelete);

        Person updatedPerson = new Person(
                personToUpdate.getName(),
                personToUpdate.getPhone(),
                personToUpdate.getEmail(),
                personToUpdate.getAddress(),
                personToUpdate.getStatus(),
                personToUpdate.getTags(),
                personToUpdate.getFollowUp(),
                personToUpdate.getLastContact(),
                updatedNotes);

        model.setPerson(personToUpdate, updatedPerson);

        return new CommandResult(
                String.format(
                        MESSAGE_SUCCESS,
                        personToUpdate.getName(),
                        noteIndex.getOneBased()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof DeleteNoteCommand otherCommand
                && contactIndex.equals(otherCommand.contactIndex)
                && noteIndex.equals(otherCommand.noteIndex));
    }

    @Override
    public int hashCode() {
        return Objects.hash(contactIndex, noteIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("contactIndex", contactIndex)
                .add("noteIndex", noteIndex)
                .toString();
    }
}
