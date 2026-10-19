package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PersonMatchesFiltersPredicate;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Lists contacts that match optional status and tag filters.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists all contacts or filters them by status and tag.\n"
            + "Parameters: [st/STATUS] [t/TAG]\n"
            + "Example: " + COMMAND_WORD + " st/CLIENT t/referral";
    public static final String MESSAGE_SUCCESS = "Listed all contacts.";

    private static final String MESSAGE_FILTERED_SUCCESS = "Listed %1$d %2$s with %3$s.";

    private final PersonMatchesFiltersPredicate predicate;

    /**
     * Creates a command that lists every contact.
     */
    public ListCommand() {
        this(Optional.empty(), Optional.empty());
    }

    /**
     * Creates a command that lists contacts matching every supplied filter.
     *
     * @param status Required status, or empty to accept every status.
     * @param tag Required tag, or empty to accept every tag.
     * @throws NullPointerException If either optional is null.
     */
    public ListCommand(Optional<Status> status, Optional<Tag> tag) {
        predicate = new PersonMatchesFiltersPredicate(status, tag);
    }

    /**
     * Lists matching contacts and returns feedback describing the result.
     *
     * @param model Model containing the contacts.
     * @return A result describing the contacts shown.
     * @throws NullPointerException If the model is null.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(getFeedbackMessage(model.getFilteredPersonList().size()));
    }

    /**
     * Returns feedback for the configured filters and result count.
     */
    private String getFeedbackMessage(int numberOfContacts) {
        Optional<Status> status = predicate.getStatus();
        Optional<String> tagName = predicate.getTagName();

        if (status.isEmpty() && tagName.isEmpty()) {
            return MESSAGE_SUCCESS;
        }

        String filterDescription;
        if (status.isPresent() && tagName.isPresent()) {
            filterDescription = "status " + status.get() + " and tag " + tagName.get();
        } else if (status.isPresent()) {
            filterDescription = "status " + status.get();
        } else {
            filterDescription = "tag " + tagName.orElseThrow();
        }

        String contactWord = numberOfContacts == 1 ? "contact" : "contacts";
        return String.format(MESSAGE_FILTERED_SUCCESS, numberOfContacts, contactWord, filterDescription);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ListCommand otherCommand)) {
            return false;
        }

        return predicate.equals(otherCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
