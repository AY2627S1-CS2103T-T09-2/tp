package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

public class MainAppTest {

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

    private void assertDefaultPrefsUsed(String prefsJson) throws IOException {
        Path prefsFilePath = testFolder.resolve("preferences.json");
        Files.writeString(prefsFilePath, prefsJson);

        UserPrefs prefs = new MainApp().initPrefs(new JsonUserPrefsStorage(prefsFilePath));

        assertEquals(new UserPrefs(), prefs);
    }
}
