package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.text.Collator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.FollowUpDueByPredicate;
import seedu.address.model.person.Person;

/**
 * Lists contacts whose follow-up is due within a number of days, earliest first.
 */
public class DueCommand extends Command {

    public static final String COMMAND_WORD = "due";
    public static final int MAX_DAYS = 365;

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Lists contacts with a follow-up due today or within the next DAYS days, earliest first. "
            + "Overdue follow-ups are included.\n"
            + "Parameters: [DAYS] (0 to " + MAX_DAYS + ", default 0)\n"
            + "Example: " + COMMAND_WORD + " 7";

    public static final String MESSAGE_SUCCESS = "%1$d %2$s due by %3$s.";
    public static final String MESSAGE_NONE_DUE = "No follow-ups due by %1$s.";

    private static final Comparator<Person> BY_FOLLOW_UP_DATE = Comparator
            .comparing((Person person) -> person.getFollowUp().map(followUp -> followUp.value).orElse(LocalDate.MAX))
            // A collator puts accented names next to their base letter (Emile, Émile, Zack) instead of after Z.
            .thenComparing(person -> person.getName().fullName, Collator.getInstance(Locale.ROOT));

    private final int days;
    private final Clock clock;

    /**
     * Creates a command that lists follow-ups due within {@code days} days of today.
     */
    public DueCommand(int days) {
        this(days, Clock.systemDefaultZone());
    }

    /**
     * Creates a command that lists follow-ups due within {@code days} days of the date given by {@code clock}.
     */
    public DueCommand(int days, Clock clock) {
        requireNonNull(clock);
        assert days >= 0 && days <= MAX_DAYS : "days should have been checked by the parser";
        this.days = days;
        this.clock = clock;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate cutoff = LocalDate.now(clock).plusDays(days);
        model.updateFilteredPersonList(new FollowUpDueByPredicate(cutoff), BY_FOLLOW_UP_DATE);

        int count = model.getFilteredPersonList().size();
        String cutoffText = FollowUp.format(cutoff);
        if (count == 0) {
            return new CommandResult(String.format(MESSAGE_NONE_DUE, cutoffText));
        }
        String followUpWord = count == 1 ? "follow-up" : "follow-ups";
        return new CommandResult(String.format(MESSAGE_SUCCESS, count, followUpWord, cutoffText));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof DueCommand otherCommand)) {
            return false;
        }

        return days == otherCommand.days && clock.equals(otherCommand.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(days, clock);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("days", days).toString();
    }
}
