package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import java.util.OptionalInt;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;
    private final int indexToScrollTo;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, -1);
    }

    /** Constructs a result that also scrolls the displayed contact list to a zero-based index. */
    public CommandResult(String feedbackToUser, int indexToScrollTo) {
        this(feedbackToUser, false, false, indexToScrollTo);
    }

    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit, int indexToScrollTo) {
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
        this.indexToScrollTo = indexToScrollTo;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public boolean isExit() {
        return exit;
    }

    /** Returns the zero-based contact index to scroll to, if the result targets a visible contact. */
    public OptionalInt getIndexToScrollTo() {
        return indexToScrollTo < 0 ? OptionalInt.empty() : OptionalInt.of(indexToScrollTo);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && indexToScrollTo == otherCommandResult.indexToScrollTo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, indexToScrollTo);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("indexToScrollTo", indexToScrollTo)
                .toString();
    }

}
