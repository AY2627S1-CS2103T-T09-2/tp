package seedu.address;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class MainAppTest {

    private static final GuiSettings SAVED_GUI_SETTINGS = new GuiSettings(1000, 500, 300, 100);

    @TempDir
    public Path testFolder;

    @Test
    public void initPrefs_repeatedKey_usesDefaultPreferences() throws IOException {
        // EP: a repeated key gives the default preferences instead of whichever value came last.
        assertDefaultPrefsUsed("{\"guiSettings\":{\"windowWidth\":1000.0,\"windowHeight\":500.0},"
                + "\"guiSettings\":{\"windowWidth\":800.0,\"windowHeight\":600.0}}");
        assertDefaultPrefsUsed("{\"guiSettings\":{\"windowWidth\":1000.0,\"windowWidth\":800.0,"
                + "\"windowHeight\":500.0}}");
    }

    @Test
    public void initPrefs_deepNesting_usesDefaultPreferences() throws IOException {
        // EP: deeply nested junk makes the whole file invalid, the same as for the contact data file.
        assertDefaultPrefsUsed("{\"guiSettings\":{\"windowWidth\":1000.0,\"windowHeight\":500.0},"
                + "\"junk\":" + "[".repeat(50_000) + "]".repeat(50_000) + "}");
    }

    @Test
    public void initPrefs_validFile_usesSavedPreferences() throws Exception {
        // EP: a valid preference file from an earlier run is loaded as it is.
        Path prefsFilePath = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath);
        storage.saveUserPrefs(prefsWith(SAVED_GUI_SETTINGS));

        UserPrefs prefs = new MainApp().initPrefs(storage);

        assertEquals(prefsWith(SAVED_GUI_SETTINGS), prefs);
    }

    @Test
    public void initPrefs_missingFile_usesDefaultsAndCreatesFile() throws Exception {
        // EP: first launch, so there is no preference file yet. The defaults are written out for next time.
        Path prefsFilePath = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath);

        UserPrefs prefs = new MainApp().initPrefs(storage);

        assertEquals(new UserPrefs(), prefs);
        assertTrue(Files.isRegularFile(prefsFilePath));
        assertEquals(new UserPrefs(), storage.readUserPrefs().get());
    }

    @Test
    public void initPrefs_invalidFile_replacedWithDefaults() throws Exception {
        // EP: a preference file that cannot be loaded is overwritten with the defaults, so the next launch
        // reads a valid file instead of failing again.
        Path prefsFilePath = testFolder.resolve("preferences.json");
        Files.writeString(prefsFilePath, "not json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsFilePath);

        new MainApp().initPrefs(storage);

        assertEquals(new UserPrefs(), storage.readUserPrefs().get());
    }

    @Test
    public void initPrefs_cannotSaveFile_stillUsesDefaults() throws Exception {
        // EP: the preference file cannot be written (its folder is blocked by a file). Startup carries on
        // with the defaults instead of failing.
        Path blockingFile = Files.writeString(testFolder.resolve("blocker"), "not a folder");
        Path prefsFilePath = blockingFile.resolve("preferences.json");

        UserPrefs prefs = new MainApp().initPrefs(new JsonUserPrefsStorage(prefsFilePath));

        assertEquals(new UserPrefs(), prefs);
        assertFalse(Files.exists(prefsFilePath));
    }

    @Test
    public void stop_validPrefsPath_savesCurrentPreferences() throws Exception {
        // EP: on exit, the window settings in the model are written to the preference file.
        Path prefsFilePath = testFolder.resolve("preferences.json");
        MainApp app = appWith(prefsFilePath, prefsWith(SAVED_GUI_SETTINGS));

        app.stop();

        assertEquals(prefsWith(SAVED_GUI_SETTINGS), new JsonUserPrefsStorage(prefsFilePath).readUserPrefs().get());
    }

    @Test
    public void stop_cannotSavePrefs_exitsWithoutError() throws Exception {
        // EP: a failed save on exit is only logged, so closing the app never shows an error.
        Path blockingFile = Files.writeString(testFolder.resolve("blocker"), "not a folder");
        Path prefsFilePath = blockingFile.resolve("preferences.json");
        MainApp app = appWith(prefsFilePath, prefsWith(SAVED_GUI_SETTINGS));

        assertDoesNotThrow(app::stop);
        assertFalse(Files.exists(prefsFilePath));
    }

    private void assertDefaultPrefsUsed(String prefsJson) throws IOException {
        Path prefsFilePath = testFolder.resolve("preferences.json");
        Files.writeString(prefsFilePath, prefsJson);

        UserPrefs prefs = new MainApp().initPrefs(new JsonUserPrefsStorage(prefsFilePath));

        assertEquals(new UserPrefs(), prefs);
    }

    private MainApp appWith(Path prefsFilePath, UserPrefs userPrefs) {
        MainApp app = new MainApp();
        app.storage = new StorageManager(new JsonAddressBookStorage(testFolder.resolve("addressbook.json")),
                new JsonUserPrefsStorage(prefsFilePath));
        app.model = new ModelManager(new AddressBook(), userPrefs);
        return app;
    }

    private static UserPrefs prefsWith(GuiSettings guiSettings) {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(guiSettings);
        return userPrefs;
    }
}
