package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;

import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Signals that unreadable contact data could not be backed up safely.
 */
public class DataBackupException extends DataLoadingException {

    /**
     * Creates an exception describing the source and failed backup destination.
     */
    public DataBackupException(Path dataPath, Path backupPath, Exception cause) {
        super(new IOException("Could not back up unreadable data file " + dataPath
                + " to " + backupPath + ". The original file was not changed.", cause));
    }
}
