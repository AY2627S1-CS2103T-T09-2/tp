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

    public static final String MESSAGE_SUCCESS = "%1$d %2$s due by %3$s.";
    public static final String MESSAGE_NONE_DUE = "No follow-ups due by %1$s.";

    // Collators put accented names next to their base letter (Emile, Émile, Zack) instead of after Z.
    // They ignore spaces and punctuation, so these word collators are only given single words (see
    // compareNames). Base letters, then accents, then case: each level is checked on every word before the next.
    private static final List<Collator> NAME_COLLATORS = Stream
            .of(Collator.PRIMARY, Collator.SECONDARY, Collator.TERTIARY)
            .map(DueCommand::rootCollator)
            .toList();
    // The collators treat letters with a stroke or bar (Đ, Ł, Ħ, Ø) as separate letters after Z. For the
    // base-letter pass only, each is read as its base letter, so Đặng sorts next to Dang and Łukasz next to Lukas.
    // The accent and case passes still see the original letters, so the plain letter comes first on a tie.
    private static final String STROKE_LETTERS = "\u0110\u0111\u0126\u0127\u0141\u0142\u00d8\u00f8"
            + "\u0166\u0167\u01b5\u01b6\u0197\u0268\u0243\u0180\u01e4\u01e5\u0131";
    private static final String STROKE_BASE_LETTERS = "DdHhLlOoTtZzIiBbGgi";
    // Only breaks ties between names whose words all match, e.g. "Tan, Ann" and "Tan Ann".
    private static final Collator WHOLE_NAME_COLLATOR = rootCollator(Collator.TERTIARY);
    // Spaces, commas, periods and hyphens end a word; apostrophes and slashes stay inside one (O'Brien, s/o).
    private static final Pattern WORD_SEPARATOR = Pattern.compile("[ ,.-]+");

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
     * Spaces, commas, periods and hyphens separate words, so they sort before any letter. Words are compared
     * with collators, first by base letters only (reading Đ as D, Ł as L and so on), then by accents, then by
     * case, so an accent or capital
     * in one word never outranks a different letter in a later word. Names that still tie are compared
     * as a whole and then by their raw text.
     */
    private static int compareNames(String first, String second) {
        String[] firstWords = splitWords(first);
        String[] secondWords = splitWords(second);
        for (Collator collator : NAME_COLLATORS) {
            boolean isBaseLetterPass = collator.getStrength() == Collator.PRIMARY;
            int result = isBaseLetterPass
                    ? compareWords(collator, foldStrokeLetters(firstWords), foldStrokeLetters(secondWords))
                    : compareWords(collator, firstWords, secondWords);
            if (result != 0) {
                return result;
            }
        }
        int result = WHOLE_NAME_COLLATOR.compare(first, second);
        // No two different valid names tie on the collator today, but if one ever did (say, after a change
        // to the name rules), comparing the raw text keeps the order total and independent of insertion order.
        return result != 0 ? result : first.compareTo(second);
    }

    /**
     * Returns {@code words} with each letter in {@link #STROKE_LETTERS} replaced by its base letter,
     * e.g. "Đặng" becomes "Dặng".
     */
    private static String[] foldStrokeLetters(String[] words) {
        String[] folded = new String[words.length];
        for (int i = 0; i < words.length; i++) {
            char[] letters = words[i].toCharArray();
            for (int j = 0; j < letters.length; j++) {
                int strokeIndex = STROKE_LETTERS.indexOf(letters[j]);
                if (strokeIndex >= 0) {
                    letters[j] = STROKE_BASE_LETTERS.charAt(strokeIndex);
                }
            }
            folded[i] = new String(letters);
        }
        return folded;
    }

    private static String[] splitWords(String name) {
        return WORD_SEPARATOR.splitAsStream(name).filter(word -> !word.isEmpty()).toArray(String[]::new);
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
