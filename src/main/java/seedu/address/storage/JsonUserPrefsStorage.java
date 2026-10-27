package seedu.address.storage;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * A class to access UserPrefs stored on the hard disk as a JSON file
 */
public class JsonUserPrefsStorage {

    /**
     * Largest window coordinate magnitude accepted from the file. No real screen layout comes close,
     * while values near the int limits make the native window code abort the app on launch.
     */
    static final int MAX_WINDOW_COORDINATE = 1_000_000;

    /**
     * Largest window width or height accepted from the file. A larger size combined with an ordinary
     * position also pushes the native window frame past the int range and aborts the app on launch.
     */
    static final int MAX_WINDOW_LENGTH = 1_000_000;

    static final String MESSAGE_MISSING_GUI_SETTINGS = "guiSettings is null";
    static final String MESSAGE_INVALID_WINDOW_SIZE =
            "window width and height must be positive numbers no larger than " + MAX_WINDOW_LENGTH;
    static final String MESSAGE_INVALID_WINDOW_COORDINATES =
            "window coordinates must be between -" + MAX_WINDOW_COORDINATE + " and " + MAX_WINDOW_COORDINATE;

    private static final Logger logger = LogsCenter.getLogger(JsonUserPrefsStorage.class);

    private Path filePath;

    public JsonUserPrefsStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getUserPrefsFilePath() {
        return filePath;
    }

    /**
     * Returns UserPrefs data from storage.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if the loading of data from preference file failed.
     */
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return readUserPrefs(filePath);
    }

    /**
     * Similar to {@link #readUserPrefs()}
     * @param prefsFilePath location of the data. Cannot be null.
     * @throws DataLoadingException if the file format is not as expected or the GUI settings are unusable.
     */
    public Optional<UserPrefs> readUserPrefs(Path prefsFilePath) throws DataLoadingException {
        Optional<UserPrefs> userPrefs = JsonUtil.readJsonFile(prefsFilePath, UserPrefs.class);
        if (userPrefs.isEmpty()) {
            return userPrefs;
        }

        try {
            checkGuiSettings(userPrefs.get().getGuiSettings());
        } catch (IllegalValueException ive) {
            logger.warning("Illegal values found in " + prefsFilePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        }
        return userPrefs;
    }

    /**
     * Checks that {@code guiSettings} read from the file can be applied to the main window.
     * Jackson fills fields directly, so a hand-edited file can bypass the checks in {@link UserPrefs}.
     *
     * @throws IllegalValueException if {@code guiSettings} is null or holds an unusable size or position.
     */
    private static void checkGuiSettings(GuiSettings guiSettings) throws IllegalValueException {
        if (guiSettings == null) {
            throw new IllegalValueException(MESSAGE_MISSING_GUI_SETTINGS);
        }
        if (!isValidWindowLength(guiSettings.getWindowWidth())
                || !isValidWindowLength(guiSettings.getWindowHeight())) {
            throw new IllegalValueException(MESSAGE_INVALID_WINDOW_SIZE);
        }

        Point coordinates = guiSettings.getWindowCoordinates();
        if (coordinates != null
                && (!isValidWindowCoordinate(coordinates.x) || !isValidWindowCoordinate(coordinates.y))) {
            throw new IllegalValueException(MESSAGE_INVALID_WINDOW_COORDINATES);
        }
    }

    private static boolean isValidWindowLength(double length) {
        return Double.isFinite(length) && length > 0 && length <= MAX_WINDOW_LENGTH;
    }

    private static boolean isValidWindowCoordinate(int coordinate) {
        return Math.abs((long) coordinate) <= MAX_WINDOW_COORDINATE;
    }

    /**
     * Saves the given {@link seedu.address.model.ReadOnlyUserPrefs} to the storage.
     * @param userPrefs cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        JsonUtil.saveJsonFile(userPrefs, filePath);
    }

}
