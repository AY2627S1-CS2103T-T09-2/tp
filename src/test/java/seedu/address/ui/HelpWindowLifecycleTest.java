package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Tests native Help window lifecycle behavior with a graphical display.
 * Set RUN_JAVAFX_TESTS=true and rerun the test task to enable these tests; ordinary CI needs no display.
 */
@EnabledIfEnvironmentVariable(named = "RUN_JAVAFX_TESTS", matches = "true")
public class HelpWindowLifecycleTest {

    private static final double PIXEL_TOLERANCE = 2;

    @TempDir
    private Path temporaryFolder;

    @BeforeAll
    public static void startToolkit() throws Exception {
        FutureTask<Void> startup = new FutureTask<>(() -> Platform.setImplicitExit(false), null);
        Platform.startup(startup);
        startup.get(10, TimeUnit.SECONDS);
    }

    @AfterEach
    public void closeWindows() throws Exception {
        runOnFxThread(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
    }

    @AfterAll
    public static void stopToolkit() {
        Platform.exit();
    }

    @Test
    public void handleHelp_firstOpening_usesMainWindowAsOwner() throws Exception {
        // EP: Help is opened through the actual main-window controller for the first time.
        runOnFxThread(() -> {
            MainWindow mainWindow = createMainWindow();
            mainWindow.handleHelp();

            Stage help = getShowingHelpWindow();
            assertSame(mainWindow.getPrimaryStage(), help.getOwner());
            assertTrue(mainWindow.getPrimaryStage().isShowing());
            assertTrue(help.isShowing());
            Scene scene = help.getScene();
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertTrue(scene.getWidth() > 0);
            assertTrue(scene.getHeight() > 0);
            assertEquals(scene.getWidth(), scene.getRoot().getLayoutBounds().getWidth(), PIXEL_TOLERANCE);
            assertEquals(scene.getHeight(), scene.getRoot().getLayoutBounds().getHeight(), PIXEL_TOLERANCE);
        });
    }

    @Test
    public void handleHelp_repeatedOpenings_preservesOwnerAndWindowBounds() throws Exception {
        // EP: an already visible Help is focused, then a moved/resized Help is closed and reopened twice.
        runOnFxThread(() -> {
            MainWindow mainWindow = createMainWindow();
            mainWindow.handleHelp();
            Stage help = getShowingHelpWindow();
            mainWindow.handleHelp();
            assertSame(help, getShowingHelpWindow());
            Rectangle2D resizedBounds = getResizedBounds();
            help.setX(resizedBounds.getMinX());
            help.setY(resizedBounds.getMinY());
            help.setWidth(resizedBounds.getWidth());
            help.setHeight(resizedBounds.getHeight());
            Rectangle2D expectedBounds = getBounds(help);

            for (int i = 0; i < 2; i++) {
                help.hide();
                mainWindow.handleHelp();
                assertSame(help, getShowingHelpWindow());
                assertSame(mainWindow.getPrimaryStage(), help.getOwner());
                assertBounds(expectedBounds, help);
            }
        });
    }

    @Test
    public void show_boundsAdjustedWhileShowing_doesNotRestoreStaleBounds() throws Exception {
        // EP: showing the native window adjusts its bounds; do not overwrite them with the hidden-window bounds.
        runOnFxThread(() -> {
            HelpWindow help = new HelpWindow();
            Stage stage = help.getRoot();
            Rectangle2D adjustedBounds = getResizedBounds();
            stage.setOnShown(event -> {
                stage.setX(adjustedBounds.getMinX());
                stage.setY(adjustedBounds.getMinY());
                stage.setWidth(adjustedBounds.getWidth());
                stage.setHeight(adjustedBounds.getHeight());
            });

            help.show();

            assertBounds(adjustedBounds, stage);
        });
    }

    private MainWindow createMainWindow() {
        Path dataFile = temporaryFolder.resolve("addressbook.json");
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFile),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        MainWindow mainWindow = new MainWindow(new Stage(), new LogicManager(new ModelManager(), storage), dataFile);
        mainWindow.show();
        return mainWindow;
    }

    private Stage getShowingHelpWindow() {
        List<Stage> windows = Window.getWindows().stream()
                .filter(Stage.class::isInstance).map(Stage.class::cast)
                .filter(stage -> "Help".equals(stage.getTitle())).toList();
        assertEquals(1, windows.size());
        return windows.get(0);
    }

    private Rectangle2D getBounds(Stage stage) {
        return new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
    }

    private Rectangle2D getResizedBounds() {
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        Dimension2D size = HelpWindow.getWindowSize(800, 600, screen);
        double x = screen.getMinX() + Math.min(20, screen.getWidth() - size.getWidth());
        double y = screen.getMinY() + Math.min(20, screen.getHeight() - size.getHeight());
        return new Rectangle2D(x, y, size.getWidth(), size.getHeight());
    }

    private void assertBounds(Rectangle2D expected, Stage stage) {
        assertEquals(expected.getMinX(), stage.getX(), PIXEL_TOLERANCE);
        assertEquals(expected.getMinY(), stage.getY(), PIXEL_TOLERANCE);
        assertEquals(expected.getWidth(), stage.getWidth(), PIXEL_TOLERANCE);
        assertEquals(expected.getHeight(), stage.getHeight(), PIXEL_TOLERANCE);
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(10, TimeUnit.SECONDS);
    }
}
