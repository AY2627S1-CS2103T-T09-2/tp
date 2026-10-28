package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.storage.JsonUserPrefsStorage.MAX_WINDOW_COORDINATE;
import static seedu.address.storage.JsonUserPrefsStorage.MAX_WINDOW_LENGTH;
import static seedu.address.storage.JsonUserPrefsStorage.MESSAGE_INVALID_WINDOW_COORDINATES;
import static seedu.address.storage.JsonUserPrefsStorage.MESSAGE_INVALID_WINDOW_SIZE;
import static seedu.address.storage.JsonUserPrefsStorage.MESSAGE_MISSING_GUI_SETTINGS;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.UserPrefs;

public class JsonUserPrefsStorageTest {

    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonUserPrefsStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readUserPrefs_nullFilePath_throwsNullPointerException() {
        // EP: no path is supplied, which is a caller error.
        assertThrows(NullPointerException.class, () -> readUserPrefs(null));
    }

    private Optional<UserPrefs> readUserPrefs(String userPrefsFileInTestDataFolder) throws DataLoadingException {
        Path prefsFilePath = addToTestDataPathIfNotNull(userPrefsFileInTestDataFolder);
        return new JsonUserPrefsStorage(prefsFilePath).readUserPrefs(prefsFilePath);
    }

    @Test
    public void readUserPrefs_missingFile_emptyResult() throws DataLoadingException {
        // EP: a valid path points to no file, as on first launch.
        assertFalse(readUserPrefs("NonExistentFile.json").isPresent());
    }

    @Test
    public void readUserPrefs_notJsonFormat_exceptionThrown() {
        // EP: the file exists but its contents are not valid JSON.
        assertThrows(DataLoadingException.class, () -> readUserPrefs("NotJsonFormatUserPrefs.json"));
    }

    private Path addToTestDataPathIfNotNull(String userPrefsFileInTestDataFolder) {
        return userPrefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(userPrefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void readUserPrefs_fileInOrder_successfullyRead() throws DataLoadingException {
        // EP: valid JSON supplies the recognized preference fields.
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("TypicalUserPref.json").get();
        assertEquals(expected, actual);
    }

    @Test
    public void readUserPrefs_valuesMissingFromFile_defaultValuesUsed() throws DataLoadingException {
        // EP: valid JSON omits recognized fields; defaults fill the missing values.
        UserPrefs actual = readUserPrefs("EmptyUserPrefs.json").get();
        assertEquals(new UserPrefs(), actual);
    }

    @Test
    public void readUserPrefs_extraValuesInFile_extraValuesIgnored() throws DataLoadingException {
        // EP: valid JSON contains unknown fields alongside recognized fields.
        UserPrefs expected = getTypicalUserPrefs();
        UserPrefs actual = readUserPrefs("ExtraValuesUserPref.json").get();

        assertEquals(expected, actual);
    }

    @Test
    public void readUserPrefs_nullGuiSettings_exceptionThrown() {
        // EP: valid JSON sets guiSettings to null, which used to crash every launch.
        assertInvalidGuiSettings(readUserPrefsThrows("NullGuiSettingsUserPrefs.json"), MESSAGE_MISSING_GUI_SETTINGS);
    }

    @Test
    public void readUserPrefs_emptyGuiSettings_defaultValuesUsed() throws Exception {
        // EP: guiSettings is present but empty; every window value falls back to its default.
        assertEquals(new UserPrefs(), readUserPrefsJson("{\"guiSettings\": {}}").get());
    }

    @Test
    public void readUserPrefs_nullWindowCoordinates_defaultPositionUsed() throws Exception {
        // EP: a null position is valid and means the window opens wherever the OS places it.
        UserPrefs actual = readUserPrefsJson("{\"guiSettings\": {\"windowWidth\": 1000, \"windowHeight\": 500,"
                + " \"windowCoordinates\": null}}").get();
        assertEquals(1000, actual.getGuiSettings().getWindowWidth());
        assertEquals(500, actual.getGuiSettings().getWindowHeight());
        assertNull(actual.getGuiSettings().getWindowCoordinates());
    }

    @Test
    public void readUserPrefs_invalidWindowSize_exceptionThrown() {
        // EP: null sizes are read as 0, which is not a usable window size.
        assertInvalidWindowSize("\"windowWidth\": null, \"windowHeight\": null");
        // Boundary: zero is the smallest invalid size.
        assertInvalidWindowSize("\"windowWidth\": 0, \"windowHeight\": 500");
        assertInvalidWindowSize("\"windowWidth\": 1000, \"windowHeight\": 0");
        // EP: negative sizes.
        assertInvalidWindowSize("\"windowWidth\": -500, \"windowHeight\": 500");
        assertInvalidWindowSize("\"windowWidth\": 1000, \"windowHeight\": -0.5");
        // EP: non-finite sizes that Jackson accepts as strings.
        assertInvalidWindowSize("\"windowWidth\": \"NaN\", \"windowHeight\": 500");
        assertInvalidWindowSize("\"windowWidth\": 1000, \"windowHeight\": \"Infinity\"");
        assertInvalidWindowSize("\"windowWidth\": \"-Infinity\", \"windowHeight\": 500");
        // Boundary: just above the largest accepted size, and huge sizes that abort the native window code.
        assertInvalidWindowSize("\"windowWidth\": " + (MAX_WINDOW_LENGTH + 1) + ", \"windowHeight\": 500");
        assertInvalidWindowSize("\"windowWidth\": 800, \"windowHeight\": 1e308");
        assertInvalidWindowSize("\"windowWidth\": " + Integer.MAX_VALUE + ", \"windowHeight\": 500");
    }

    @Test
    public void readUserPrefs_validWindowSizeBoundaries_successfullyRead() throws Exception {
        // Boundary: the smallest positive size and the largest accepted size; the window clamps them to the screen.
        assertWindowSizeRead(Double.MIN_VALUE, MAX_WINDOW_LENGTH);
        assertWindowSizeRead(MAX_WINDOW_LENGTH, Double.MIN_VALUE);
    }

    @Test
    public void readUserPrefs_windowCoordinateBoundaries_outOfRangeRejected() throws Exception {
        // Boundary: the largest accepted coordinates on both sides.
        assertWindowCoordinatesRead(MAX_WINDOW_COORDINATE, -MAX_WINDOW_COORDINATE);
        assertWindowCoordinatesRead(-MAX_WINDOW_COORDINATE, MAX_WINDOW_COORDINATE);
        // Boundary: just outside the accepted range.
        assertInvalidWindowCoordinates(MAX_WINDOW_COORDINATE + 1, 0);
        assertInvalidWindowCoordinates(0, -MAX_WINDOW_COORDINATE - 1);
        // EP: int extremes, which make the native window code abort the app.
        assertInvalidWindowCoordinates(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertInvalidWindowCoordinates(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Test
    public void readUserPrefs_invalidGuiSettingsType_exceptionThrown() {
        // EP: guiSettings has the wrong JSON type, so Jackson cannot map it.
        assertThrows(DataLoadingException.class, () -> readUserPrefsJson("{\"guiSettings\": \"\"}"));
        assertThrows(DataLoadingException.class, () -> readUserPrefsJson("{\"guiSettings\": []}"));
        assertThrows(DataLoadingException.class, () ->
                readUserPrefsJson("{\"guiSettings\": {\"windowWidth\": \"wide\"}}"));
    }

    @Test
    public void readUserPrefs_invalidGuiSettings_defaultsCanBeSavedAndReadBack() throws Exception {
        // EP: after a rejected file, saving defaults replaces it with a file that reads back cleanly.
        Path prefsFilePath = testFolder.resolve("prefs.json");
        Files.writeString(prefsFilePath, "{\"guiSettings\": null}");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath);
        assertThrows(DataLoadingException.class, storage::readUserPrefs);

        storage.saveUserPrefs(new UserPrefs());
        assertFalse(Files.readString(prefsFilePath).matches("(?s).*\"guiSettings\"\\s*:\\s*null.*"));
        assertEquals(new UserPrefs(), storage.readUserPrefs().get());
    }

    private Optional<UserPrefs> readUserPrefsJson(String json) throws IOException, DataLoadingException {
        Path prefsFilePath = testFolder.resolve("prefs.json");
        Files.writeString(prefsFilePath, json);
        return new JsonUserPrefsStorage(prefsFilePath).readUserPrefs();
    }

    private DataLoadingException readUserPrefsThrows(String userPrefsFileInTestDataFolder) {
        return org.junit.jupiter.api.Assertions.assertThrows(DataLoadingException.class, () ->
                readUserPrefs(userPrefsFileInTestDataFolder));
    }

    private DataLoadingException readUserPrefsJsonThrows(String json) {
        return org.junit.jupiter.api.Assertions.assertThrows(DataLoadingException.class, () ->
                readUserPrefsJson(json));
    }

    private void assertInvalidGuiSettings(DataLoadingException e, String expectedMessage) {
        assertInstanceOf(IllegalValueException.class, e.getCause());
        assertEquals(expectedMessage, e.getCause().getMessage());
    }

    private void assertInvalidWindowSize(String sizeFields) {
        assertInvalidGuiSettings(readUserPrefsJsonThrows("{\"guiSettings\": {" + sizeFields + "}}"),
                MESSAGE_INVALID_WINDOW_SIZE);
    }

    private void assertWindowSizeRead(double width, double height) throws Exception {
        UserPrefs actual = readUserPrefsJson("{\"guiSettings\": {\"windowWidth\": " + width
                + ", \"windowHeight\": " + height + "}}").get();
        assertEquals(width, actual.getGuiSettings().getWindowWidth());
        assertEquals(height, actual.getGuiSettings().getWindowHeight());
    }

    private String coordinatesJson(int x, int y) {
        return "{\"guiSettings\": {\"windowWidth\": 1000, \"windowHeight\": 500,"
                + " \"windowCoordinates\": {\"x\": " + x + ", \"y\": " + y + "}}}";
    }

    private void assertWindowCoordinatesRead(int x, int y) throws Exception {
        UserPrefs expected = new UserPrefs();
        expected.setGuiSettings(new GuiSettings(1000, 500, x, y));
        assertEquals(expected, readUserPrefsJson(coordinatesJson(x, y)).get());
    }

    private void assertInvalidWindowCoordinates(int x, int y) {
        assertInvalidGuiSettings(readUserPrefsJsonThrows(coordinatesJson(x, y)), MESSAGE_INVALID_WINDOW_COORDINATES);
    }

    private UserPrefs getTypicalUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1000, 500, 300, 100));
        return userPrefs;
    }

    @Test
    public void savePrefs_nullPrefs_throwsNullPointerException() {
        // EP: the preferences object is null while the path is supplied.
        assertThrows(NullPointerException.class, () -> saveUserPrefs(null, "SomeFile.json"));
    }

    @Test
    public void saveUserPrefs_nullFilePath_throwsNullPointerException() {
        // EP: the save path is null while the preferences object is supplied.
        assertThrows(NullPointerException.class, () -> saveUserPrefs(new UserPrefs(), null));
    }

    /**
     * Saves {@code userPrefs} at the specified {@code prefsFileInTestDataFolder} filepath.
     */
    private void saveUserPrefs(UserPrefs userPrefs, String prefsFileInTestDataFolder) {
        try {
            new JsonUserPrefsStorage(addToTestDataPathIfNotNull(prefsFileInTestDataFolder))
                    .saveUserPrefs(userPrefs);
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file", ioe);
        }
    }

    @Test
    public void saveUserPrefs_allInOrder_success() throws DataLoadingException, IOException {

        UserPrefs original = new UserPrefs();
        original.setGuiSettings(new GuiSettings(1200, 200, 0, 2));

        Path pefsFilePath = testFolder.resolve("TempPrefs.json");
        JsonUserPrefsStorage jsonUserPrefsStorage = new JsonUserPrefsStorage(pefsFilePath);

        // EP: the destination is absent; saving creates it.
        jsonUserPrefsStorage.saveUserPrefs(original);
        UserPrefs readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);

        // EP: the destination already exists; saving replaces the previous preferences.
        original.setGuiSettings(new GuiSettings(5, 5, 5, 5));
        jsonUserPrefsStorage.saveUserPrefs(original);
        readBack = jsonUserPrefsStorage.readUserPrefs().get();
        assertEquals(original, readBack);
    }

}
