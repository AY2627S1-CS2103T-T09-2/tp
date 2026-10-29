package seedu.address;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Builds the message shown in the result box at startup when the data file could not be loaded.
 */
public final class StartupNotice {

    public static final String MESSAGE_DATA_FILE_NOT_LOADED = "Could not load %1$s. A copy was saved as %2$s.\n"
            + "To fix the file yourself, close the app now; your next change replaces it.\n"
            + "Reason: %3$s";

    public static final String MESSAGE_UNKNOWN_REASON = "The file is not in the expected format.";
    // Kept to one line each, so that the whole notice fits in the result box at the default window size.
    public static final String MESSAGE_FILE_ENDS_EARLY =
            "The file ends too early; check that every bracket and quote is closed.";
    public static final String MESSAGE_NOT_VALID_JSON =
            "The file is not valid JSON%1$s; check the commas, quotes and brackets.";
    public static final String MESSAGE_UNEXPECTED_LAYOUT = "Wrong kind of value%1$s, e.g. text where a list should be.";
    public static final String MESSAGE_NEAR_LINE = " near line %1$d";

    // Jackson 2.7 has no separate exception type for a file that stops in the middle of a value.
    private static final String JACKSON_END_OF_INPUT = "Unexpected end-of-input";

    private StartupNotice() {}

    /**
     * Returns the notice for a data file at {@code dataFilePath} that failed to load with {@code error}
     * and was copied to its backup path. Paths are shown the same way as in the status bar.
     */
    public static String forUnloadableDataFile(Path dataFilePath, DataLoadingException error) {
        requireNonNull(dataFilePath);
        requireNonNull(error);
        Path shownPath = Paths.get(".").resolve(dataFilePath);
        return String.format(MESSAGE_DATA_FILE_NOT_LOADED, shownPath,
                JsonAddressBookStorage.getBackupFilePath(shownPath), getReason(error));
    }

    /**
     * Returns the reason for {@code error}. Our own messages, such as an invalid contact or a repeated
     * key, are shown as they are. Jackson's messages name internal classes and quote the file, so they
     * are replaced with a plain reason. Only the line is given, because Jackson's column is often off.
     */
    static String getReason(DataLoadingException error) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        if (cause instanceof JsonParseException parseError) {
            String message = parseError.getOriginalMessage();
            if (message != null && message.startsWith(JACKSON_END_OF_INPUT)) {
                // The location is past the end of the file, and Jackson 2.7 can even get its column wrong.
                return MESSAGE_FILE_ENDS_EARLY;
            }
            return String.format(MESSAGE_NOT_VALID_JSON, nearLine(parseError));
        }
        if (cause instanceof JsonProcessingException mappingError) {
            return String.format(MESSAGE_UNEXPECTED_LAYOUT, nearLine(mappingError));
        }
        String reason = cause.getMessage() == null ? "" : cause.getMessage().strip();
        return reason.isEmpty() ? MESSAGE_UNKNOWN_REASON : reason;
    }

    /**
     * Returns " near line N" for the line where Jackson found the problem, or an empty string if it is unknown.
     */
    private static String nearLine(JsonProcessingException error) {
        JsonLocation location = error.getLocation();
        if (location == null || location.getLineNr() <= 0) {
            return "";
        }
        return String.format(MESSAGE_NEAR_LINE, location.getLineNr());
    }
}
