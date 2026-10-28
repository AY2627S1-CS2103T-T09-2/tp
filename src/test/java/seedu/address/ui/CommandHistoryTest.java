package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class CommandHistoryTest {

    @Test
    public void navigate_emptyHistory_preservesCurrentInput() {
        // EP: there are no submitted commands; both boundaries coincide.
        CommandHistory history = new CommandHistory();
        for (String input : List.of("", "add n/unfinished", "  ")) {
            assertEquals(input, history.previous(input));
            assertEquals(input, history.next(input));
        }
    }

    @Test
    public void navigate_oneEntry_restoresDraftWithoutWrapping() {
        // EP: a single entry is both the oldest and newest command.
        CommandHistory history = new CommandHistory();
        history.add("list");

        assertEquals("list", history.previous("add n/Alice"));
        assertEquals("list", history.previous("list"));
        assertEquals("add n/Alice", history.next("list"));
        assertEquals("add n/Alice", history.next("add n/Alice"));
    }

    @Test
    public void navigate_newestMatchesInput_recallsOlderCommandAndRestoresInput() {
        // EP: a failed command remains in the input; the first Up recalls an older entry.
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("invalid command");

        assertEquals("list", history.previous("invalid command"));
        assertEquals("invalid command", history.next("list"));
        assertEquals("invalid command", history.next("invalid command"));

        // EP: starting another browsing cycle skips the matching newest entry again.
        assertEquals("list", history.previous("invalid command"));
    }

    @Test
    public void navigate_onlyEntryMatchesInput_preservesInputWithoutWrapping() {
        // EP: the only entry matches the input; there is no older entry to recall.
        CommandHistory history = new CommandHistory();
        history.add("invalid command");

        assertEquals("invalid command", history.previous("invalid command"));
        assertEquals("invalid command", history.previous("invalid command"));
        assertEquals("invalid command", history.next("invalid command"));
        assertEquals("invalid command", history.next("invalid command"));
    }

    @Test
    public void navigate_multipleEntries_recallsInSubmissionOrder() {
        // EP: multiple entries allow movement in both directions between the boundaries.
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("find Alice");
        history.add("due 7");

        assertEquals("due 7", history.previous("draft"));
        assertEquals("find Alice", history.previous("due 7"));
        assertEquals("list", history.previous("find Alice"));
        assertEquals("list", history.previous("list"));
        assertEquals("find Alice", history.next("list"));
        assertEquals("due 7", history.next("find Alice"));
        assertEquals("draft", history.next("due 7"));
        assertEquals("draft", history.next("draft"));
    }

    @Test
    public void navigate_editedRecalledCommands_preservesEntriesAndOriginalDraft() {
        // EP: editing recalled text does not overwrite stored entries or the draft saved before browsing.
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("find Alice");

        assertEquals("find Alice", history.previous("add n/unfinished"));
        assertEquals("list", history.previous("find Bob"));
        assertEquals("find Alice", history.next("list st/CLIENT"));
        assertEquals("add n/unfinished", history.next("find Carol"));

        // EP: a later browsing cycle saves the newly edited draft instead of restoring a stale one.
        assertEquals("find Alice", history.previous("add n/updated"));
        assertEquals("add n/updated", history.next("find Alice"));
    }

    @Test
    public void navigate_editedTextAtBoundaries_preservesCurrentInput() {
        // EP: pressing past either boundary leaves edits intact and does not wrap.
        CommandHistory history = new CommandHistory();
        history.add("list");

        assertEquals("new draft", history.next("new draft"));
        assertEquals("list", history.previous("new draft"));
        assertEquals("list st/CLIENT", history.previous("list st/CLIENT"));
        assertEquals("new draft", history.next("list st/CLIENT"));
        assertEquals("edited draft", history.next("edited draft"));
    }

    @Test
    public void add_blankCommands_ignoresEntriesWithoutResettingNavigation() {
        // EP: empty and whitespace-only submissions are ignored, including while browsing.
        CommandHistory history = new CommandHistory();
        for (String command : List.of("", " ", "\t\n", "\u2003")) {
            history.add(command);
        }
        assertEquals("draft", history.previous("draft"));

        history.add("list");
        history.add("find Alice");
        assertEquals("find Alice", history.previous("draft"));
        history.add("   ");
        assertEquals("list", history.previous("find Alice"));
        assertEquals("find Alice", history.next("list"));
        assertEquals("draft", history.next("find Alice"));
    }

    @Test
    public void add_nonBlankCommands_preservesExactTextAndDuplicates() {
        // EP: commands retain whitespace, Unicode, casing and repeated submissions without normalization.
        String command = "  FiNd Jose\u0301  ";
        CommandHistory history = new CommandHistory();
        history.add(command);
        history.add(command);

        assertEquals(command, history.previous("draft"));
        assertEquals(command, history.previous(command));
        assertEquals(command, history.next(command));
        assertEquals("draft", history.next(command));
    }

    @Test
    public void add_whileBrowsing_resetsNavigationAndDiscardsOldDraft() {
        // EP: submitting an edited recalled command starts a new navigation cycle at the newest entry.
        CommandHistory history = new CommandHistory();
        history.add("list");
        history.add("find Alice");
        assertEquals("find Alice", history.previous("old draft"));
        assertEquals("list", history.previous("find Alice"));

        history.add("list st/CLIENT");

        assertEquals("list st/CLIENT", history.previous(""));
        assertEquals("", history.next("list st/CLIENT"));
        assertEquals("list st/CLIENT", history.previous("new draft"));
        assertEquals("find Alice", history.previous("list st/CLIENT"));
    }

    @Test
    public void add_atCapacityBoundaries_retainsOnlyNewestHundredEntries() {
        // EP: below capacity, exactly at capacity, first eviction, and repeated evictions.
        for (int count : List.of(99, 100, 101, 250)) {
            CommandHistory history = new CommandHistory();
            for (int i = 1; i <= count; i++) {
                history.add("find Contact" + i);
            }
            history.add(" "); // Blank submissions must not evict an entry at capacity.

            int oldestRetained = Math.max(1, count - 99);
            String currentInput = "draft";
            for (int i = count; i >= oldestRetained; i--) {
                currentInput = history.previous(currentInput);
                assertEquals("find Contact" + i, currentInput);
            }
            assertEquals(currentInput, history.previous(currentInput));

            for (int i = oldestRetained + 1; i <= count; i++) {
                currentInput = history.next(currentInput);
                assertEquals("find Contact" + i, currentInput);
            }
            assertEquals("draft", history.next(currentInput));
            assertEquals("draft", history.next("draft"));
        }
    }

    @Test
    public void constructor_newSession_hasIndependentEmptyHistory() {
        // EP: creating a new session does not share commands or navigation state with an earlier session.
        CommandHistory earlierSession = new CommandHistory();
        earlierSession.add("list");
        assertEquals("list", earlierSession.previous("old draft"));

        CommandHistory newSession = new CommandHistory();
        assertEquals("new draft", newSession.previous("new draft"));
        assertEquals("new draft", newSession.next("new draft"));
        assertEquals("old draft", earlierSession.next("list"));
    }

    @Test
    public void addOrNavigate_nullInput_throwsNullPointerException() {
        // EP: null is a programming error rather than blank user input.
        CommandHistory history = new CommandHistory();
        assertThrows(NullPointerException.class, () -> history.add(null));
        assertThrows(NullPointerException.class, () -> history.previous(null));
        assertThrows(NullPointerException.class, () -> history.next(null));
    }
}
