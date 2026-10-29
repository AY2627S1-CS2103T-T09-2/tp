package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.text.Collator;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

    // Collators put accented names next to their base letter (Emile, Émile, Zack) instead of after Z.
    // They are only used on single words because they ignore spaces and hyphens (see compareNames).
    // Base letters, then accents, then case: each level is checked across the whole name before the next.
    private static final List<Collator> NAME_COLLATORS = Stream
            .of(Collator.PRIMARY, Collator.SECONDARY, Collator.TERTIARY)
            .map(DueCommand::rootCollator)
            .toList();
    private static final Collator WHOLE_NAME_COLLATOR = rootCollator(Collator.TERTIARY);
    private static final Pattern WORD_SEPARATOR = Pattern.compile("[ -]+");

    private static final Comparator<Person> BY_FOLLOW_UP_DATE = Comparator
            .comparing((Person person) -> person.getFollowUp().map(followUp -> followUp.value).orElse(LocalDate.MAX))
            .thenComparing(person -> person.getName().fullName, DueCommand::compareNames);

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

    private static Collator rootCollator(int strength) {
        Collator collator = Collator.getInstance(Locale.ROOT);
        collator.setStrength(strength);
        return collator;
    }

    /**
     * Compares two names word by word, so "Li Mei" comes before "Lim Bo" and "Tan Wei" before "Tanaka".
     * Spaces and hyphens separate words, which makes them sort before any letter. Words are compared
     * with collators, first by base letters only, then by accents, then by case, so an accent or capital
     * in one word never outranks a different letter in a later word. Names that still tie are compared
     * as a whole and then by their raw text, so different names never compare as equal.
     */
    private static int compareNames(String first, String second) {
        String[] firstWords = WORD_SEPARATOR.split(first);
        String[] secondWords = WORD_SEPARATOR.split(second);
        for (Collator collator : NAME_COLLATORS) {
            int result = compareWords(collator, firstWords, secondWords);
            if (result != 0) {
                return result;
            }
        }
        int result = WHOLE_NAME_COLLATOR.compare(first, second);
        return result != 0 ? result : first.compareTo(second);
    }

    /**
     * Compares two lists of words in order with {@code collator}; a name that runs out of words first
     * comes first.
     */
    private static int compareWords(Collator collator, String[] firstWords, String[] secondWords) {
        int sharedLength = Math.min(firstWords.length, secondWords.length);
        for (int i = 0; i < sharedLength; i++) {
            int result = collator.compare(firstWords[i], secondWords[i]);
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(firstWords.length, secondWords.length);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        LocalDate cutoff = LocalDate.now(clock).plusDays(days);
        model.updateFilteredPersonList(new FollowUpDueByPredicate(cutoff), BY_FOLLOW_UP_DATE);

        int count = model.getFilteredPersonList().size();
        String cutoffText = FollowUp.format(cutoff);
        if (count == 0) {
            return new CommandResult(String.format(Locale.ROOT, MESSAGE_NONE_DUE, cutoffText));
        }
        String followUpWord = count == 1 ? "follow-up" : "follow-ups";
        return new CommandResult(String.format(Locale.ROOT, MESSAGE_SUCCESS, count, followUpWord, cutoffText));
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
