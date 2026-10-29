package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
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
                new ArrayList<>(personToUpdate.getInteractionNotes());

        Collections.reverse(displayedNotes);

        if (noteIndex.getZeroBased() >= displayedNotes.size()) {
            throw new CommandException(
                    getInvalidNoteDisplayedIndexMessage(
                            noteIndex.getOneBased(),
                            displayedNotes.size()));
        }

        List<InteractionNote> updatedNotes =
                new ArrayList<>(personToUpdate.getInteractionNotes());

        updatedNotes.remove(
                updatedNotes.size() - 1 - noteIndex.getZeroBased());

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

    /**
     * Returns the error message for a note index outside the displayed history.
     */
    private static String getInvalidNoteDisplayedIndexMessage(
            int invalidIndex, int displayedCount) {
        String noteWord = displayedCount == 1 ? "note" : "notes";
        return String.format(
                "There is no interaction note %d. The history shows %d %s.",
                invalidIndex,
                displayedCount,
                noteWord);
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
