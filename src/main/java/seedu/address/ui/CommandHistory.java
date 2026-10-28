package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores up to 100 submitted commands for one session and restores unfinished input after browsing.
 * Recalling or editing a command does not change the stored entries.
 */
public class CommandHistory {

    private static final int MAX_ENTRIES = 100;

    private final List<String> entries = new ArrayList<>();
    // The index after the newest entry represents the user's unfinished input.
    private int currentIndex;
    private String unfinishedInput = "";

    /**
     * Adds a non-blank command exactly as submitted and resets navigation.
     * Removes the oldest entry when the limit is exceeded. Blank commands are ignored.
     */
    public void add(String commandText) {
        requireNonNull(commandText);
        if (commandText.isBlank()) {
            return;
        }

        entries.add(commandText);
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
        currentIndex = entries.size();
        unfinishedInput = "";
    }

    /**
     * Returns the previous command, saving the current input when browsing starts.
     * Returns the current input unchanged if there is no older command.
     */
    public String previous(String currentInput) {
        requireNonNull(currentInput);
        if (currentIndex == 0) {
            return currentInput;
        }

        if (currentIndex == entries.size()) {
            unfinishedInput = currentInput;
            if (currentIndex > 1 && entries.get(currentIndex - 1).equals(currentInput)) {
                currentIndex--;
            }
        }
        currentIndex--;
        return entries.get(currentIndex);
    }

    /**
     * Returns the next command, or restores unfinished input after the newest command.
     * Returns the current input unchanged if browsing has already ended.
     */
    public String next(String currentInput) {
        requireNonNull(currentInput);
        if (currentIndex == entries.size()) {
            return currentInput;
        }

        currentIndex++;
        return currentIndex == entries.size() ? unfinishedInput : entries.get(currentIndex);
    }
}
