package seedu.address;

import static java.util.Objects.requireNonNull;

import java.nio.file.Path;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.storage.JsonAddressBookStorage;

/**
 * Builds the message shown in the result box at startup when the data file could not be loaded.
 */
public final class StartupNotice {

    // Short lines, because the result box does not wrap text and shows about three lines at the default size.
    public static final String MESSAGE_DATA_FILE_NOT_LOADED = "Could not load %1$s. A copy was saved as %2$s.\n"
            + "To fix the file yourself, close the app now; your next change replaces it.\n"
            + "Reason: %3$s";

    public static final String MESSAGE_UNKNOWN_REASON = "The file is not in the expected format.";

    private StartupNotice() {}

    /**
     * Returns the notice for a data file at {@code dataFilePath} that failed to load with {@code error}
     * and was copied to its backup path.
     */
    public static String forUnloadableDataFile(Path dataFilePath, DataLoadingException error) {
        requireNonNull(dataFilePath);
        requireNonNull(error);
        return String.format(MESSAGE_DATA_FILE_NOT_LOADED, dataFilePath,
                JsonAddressBookStorage.getBackupFilePath(dataFilePath), getReason(error));
    }

    /**
     * Returns the reason for {@code error}. Jackson messages are cut to their first line, which leaves
     * out the parser's internal source description, and get the line and column of the problem instead.
     */
    static String getReason(DataLoadingException error) {
        Throwable cause = error.getCause() == null ? error : error.getCause();
        String reason;
        if (cause instanceof JsonProcessingException jsonError) {
            reason = firstLine(jsonError.getOriginalMessage());
            JsonLocation location = jsonError.getLocation();
            if (!reason.isEmpty() && location != null && location.getLineNr() > 0) {
                reason += String.format(" at line %d, column %d", location.getLineNr(), location.getColumnNr());
            }
        } else {
            reason = cause.getMessage() == null ? "" : cause.getMessage().strip();
        }
        return reason.isEmpty() ? MESSAGE_UNKNOWN_REASON : reason;
    }

    private static String firstLine(String message) {
        if (message == null) {
            return "";
        }
        int lineBreak = message.indexOf('\n');
        return (lineBreak < 0 ? message : message.substring(0, lineBreak)).strip();
    }
}
