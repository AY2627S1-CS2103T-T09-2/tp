package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.format.DateTimeFormatter;
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
 * Shows the interaction history of a contact.
 */
public class HistoryCommand extends Command {

    public static final String COMMAND_WORD = "history";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Shows the interaction notes of the contact identified by the displayed index.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 2";

    public static final String MESSAGE_NO_NOTES =
            "No interaction notes recorded for %1$s.";

    public static final String MESSAGE_HISTORY_HEADER =
            "Interaction history for %1$s:";

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final Index targetIndex;

    public HistoryCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
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

        Person person = lastShownList.get(targetIndex.getZeroBased());

        List<InteractionNote> notes =
                new ArrayList<>(person.getInteractionNotes());

        Collections.reverse(notes);

        if (notes.isEmpty()) {
            return new CommandResult(
                    String.format(MESSAGE_NO_NOTES, person.getName()));
        }

        StringBuilder history = new StringBuilder(
                String.format(MESSAGE_HISTORY_HEADER, person.getName()));

        for (int i = 0; i < notes.size(); i++) {
            InteractionNote note = notes.get(i);

            history.append(System.lineSeparator())
                    .append(i + 1)
                    .append(". [")
                    .append(note.getCreatedAt().format(TIMESTAMP_FORMATTER))
                    .append("] ")
                    .append(note.getValue());
        }

        return new CommandResult(history.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this
                || (other instanceof HistoryCommand otherHistoryCommand
                && targetIndex.equals(otherHistoryCommand.targetIndex));
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
