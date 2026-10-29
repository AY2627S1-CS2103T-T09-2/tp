package seedu.address.ui;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page.
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL =
            "https://ay2627s1-cs2103t-t09-2.github.io/tp/UserGuide.html";

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private static final double DEFAULT_WIDTH = 640;
    private static final double DEFAULT_HEIGHT = 540;
    private static final double MIN_WIDTH = 600;
    private static final double MIN_HEIGHT = 480;
    private static final double WINDOW_GAP = 16;
    private static final double COMPACT_LAYOUT_WIDTH = 760;

    private boolean compactLayout;

    @FXML
    private Button copyButton;

    @FXML
    private Label userGuideUrl;

    @FXML
    private Label copyStatus;

    @FXML
    private ScrollPane commandGuideScrollPane;

    @FXML
    private VBox commandGuideRows;

    @FXML
    private GridPane commandGuideTable;

    /**
     * Creates a new HelpWindow.
     *
     * @param root Stage to use as the root of the HelpWindow.
     */
    public HelpWindow(Stage root) {
        super(FXML, root);
        userGuideUrl.setText(USERGUIDE_URL);
        configureFonts();
        configureExamples();
        layoutCommandGuide(false);
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> handleCloseKey(event, this::hide));
        commandGuideScrollPane.viewportBoundsProperty().addListener((observable, oldBounds, newBounds) -> {
            boolean compact = newBounds.getWidth() < COMPACT_LAYOUT_WIDTH;
            if (newBounds.getWidth() > 0 && compact != compactLayout) {
                layoutCommandGuide(compact);
            }
        });
    }

    /**
     * Creates a new HelpWindow.
     */
    public HelpWindow() {
        this(new Stage());
    }

    /**
     * Fits read-only examples to their wrapped text so the guide remains the scrolling container.
     */
    private void configureExamples() {
        for (Node row : commandGuideRows.getChildren()) {
            for (Node cell : ((GridPane) row).getChildren()) {
                if (cell instanceof TextArea example) {
                    example.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> {
                        double textWidth = example.getWidth() - example.getInsets().getLeft()
                                - example.getInsets().getRight();
                        return getExampleTextHeight(example.getText(), example.getFont(), textWidth)
                                + example.getInsets().getTop() + example.getInsets().getBottom();
                    }, example.textProperty(), example.fontProperty(), example.widthProperty(),
                            example.insetsProperty()));
                }
            }
        }
    }

    /**
     * Returns the height needed for wrapped example text, allowing for pixel rounding and the caret.
     */
    static double getExampleTextHeight(String text, Font font, double width) {
        Text measurement = new Text(text);
        measurement.setFont(font);
        measurement.setWrappingWidth(Math.max(1, Math.floor(width)));
        return Math.ceil(measurement.getLayoutBounds().getHeight()) + 2;
    }

    /**
     * Chooses installed font families for headings and command text instead of generic fallbacks.
     */
    private void configureFonts() {
        List<String> families = Font.getFamilies();
        String uiFamily = List.of("Arial", "Liberation Sans", "DejaVu Sans", "System").stream()
                .filter(families::contains).findFirst().orElse("System");
        getRoot().getScene().getRoot().setStyle("-fx-font-family: '" + uiFamily + "';");
        String family = List.of("Menlo", "Consolas", "DejaVu Sans Mono", "Monospaced").stream()
                .filter(families::contains).findFirst().orElse("Monospaced");
        for (Node row : commandGuideRows.getChildren()) {
            for (Node cell : ((GridPane) row).getChildren()) {
                if (cell.getStyleClass().contains("command-guide-code")
                        || cell.getStyleClass().contains("command-guide-name")) {
                    cell.setStyle("-fx-font-family: '" + family + "';");
                }
            }
        }
        logger.fine(() -> "Configured command guide fonts; ui=" + uiFamily + ", code=" + family);
    }

    /**
     * Uses table columns on wide windows and labelled, stacked fields on narrow windows.
     */
    private void layoutCommandGuide(boolean compact) {
        boolean layoutChanged = compactLayout != compact;
        compactLayout = compact;
        commandGuideTable.setVisible(!compact);
        commandGuideTable.setManaged(!compact);

        for (Node node : commandGuideRows.getChildren()) {
            GridPane row = (GridPane) node;
            row.getColumnConstraints().clear();
            double[] percentages = compact ? new double[]{100} : new double[]{12, 44, 44};
            for (double percentage : percentages) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(percentage);
                row.getColumnConstraints().add(column);
            }
            if (row == commandGuideTable) {
                continue;
            }
            if (compact) {
                row.getStyleClass().add("command-guide-compact");
            } else {
                row.getStyleClass().remove("command-guide-compact");
            }
            for (Node cell : row.getChildren()) {
                boolean caption = cell.getStyleClass().contains("command-guide-caption");
                int column = 0;
                if (cell.getStyleClass().contains("command-guide-format")) {
                    column = 1;
                } else if (cell.getStyleClass().contains("command-guide-example")) {
                    column = 2;
                }
                GridPane.setColumnIndex(cell, compact ? 0 : column);
                GridPane.setRowIndex(cell, compact ? column * 2 - (caption ? 1 : 0) : 0);
                GridPane.setValignment(cell, VPos.TOP);
                if (caption) {
                    cell.setVisible(compact);
                    cell.setManaged(compact);
                }
            }
        }
        if (layoutChanged) {
            logger.fine(() -> "Command guide layout changed to " + (compact ? "stacked" : "table"));
        }
    }

    /**
     * Shows the help window.
     *
     * @throws IllegalStateException
     *     <ul>
     *         <li>
     *             if this method is called on a thread other than the JavaFX Application Thread.
     *         </li>
     *         <li>
     *             if this method is called during animation or layout processing.
     *         </li>
     *         <li>
     *             if this method is called on the primary stage.
     *         </li>
     *         <li>
     *             if {@code dialogStage} is already showing.
     *         </li>
     *     </ul>
     */
    public void show() {
        show(null);
    }

    /**
     * Shows Help beside the main window on first opening, retaining its position on later openings.
     * A null main window uses the primary screen for the initial placement.
     */
    public void show(Window mainWindow) {
        fitToScreen(mainWindow);
        copyStatus.setText("");
        Rectangle2D bounds = new Rectangle2D(getRoot().getX(), getRoot().getY(),
                getRoot().getWidth(), getRoot().getHeight());
        getRoot().show();
        // The native window manager may adjust the bounds while showing a previously hidden window.
        getRoot().setX(bounds.getMinX());
        getRoot().setY(bounds.getMinY());
        getRoot().setWidth(bounds.getWidth());
        getRoot().setHeight(bounds.getHeight());
        commandGuideScrollPane.requestFocus();
        logger.fine("Showing command guide.");
    }

    /**
     * Keeps Help within its screen's usable area without resetting a user-chosen position or size.
     */
    private void fitToScreen(Window mainWindow) {
        Stage stage = getRoot();
        double previousWidth = stage.getWidth();
        double previousHeight = stage.getHeight();
        Screen screen = Screen.getPrimary();

        if (!Double.isNaN(stage.getX()) && !Double.isNaN(stage.getY())) {
            screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1).stream()
                    .findFirst().orElse(screen);
        } else if (mainWindow != null) {
            screen = Screen.getScreensForRectangle(mainWindow.getX(), mainWindow.getY(),
                    mainWindow.getWidth(), mainWindow.getHeight()).stream().findFirst().orElse(screen);
        }

        Rectangle2D visualBounds = screen.getVisualBounds();
        Dimension2D size = getWindowSize(previousWidth, previousHeight, visualBounds);
        double width = size.getWidth();
        double height = size.getHeight();
        stage.setMinWidth(Math.min(MIN_WIDTH, visualBounds.getWidth()));
        stage.setMinHeight(Math.min(MIN_HEIGHT, visualBounds.getHeight()));
        stage.setWidth(width);
        stage.setHeight(height);
        Point2D position;
        if ((Double.isNaN(stage.getX()) || Double.isNaN(stage.getY())) && mainWindow != null) {
            Rectangle2D mainBounds = new Rectangle2D(mainWindow.getX(), mainWindow.getY(),
                    mainWindow.getWidth(), mainWindow.getHeight());
            position = getInitialPosition(mainBounds, width, height, visualBounds);
        } else {
            position = getWindowPosition(stage.getX(), stage.getY(), width, height, visualBounds);
        }
        stage.setX(position.getX());
        stage.setY(position.getY());
        if (Double.compare(previousWidth, width) != 0 || Double.compare(previousHeight, height) != 0) {
            logger.fine("Adjusted command guide size; from=" + previousWidth + "x" + previousHeight
                    + ", to=" + width + "x" + height);
        }
    }

    /**
     * Returns an initial position beside the main window, using the less occupied screen edge if neither side fits.
     */
    static Point2D getInitialPosition(Rectangle2D mainBounds, double width, double height, Rectangle2D visualBounds) {
        double rightSpace = visualBounds.getMaxX() - mainBounds.getMaxX();
        double leftSpace = mainBounds.getMinX() - visualBounds.getMinX();
        double x;
        if (rightSpace >= width + WINDOW_GAP) {
            x = mainBounds.getMaxX() + WINDOW_GAP;
        } else if (leftSpace >= width + WINDOW_GAP) {
            x = mainBounds.getMinX() - width - WINDOW_GAP;
        } else {
            x = rightSpace >= leftSpace ? visualBounds.getMaxX() - width : visualBounds.getMinX();
        }
        return getWindowPosition(x, mainBounds.getMinY() + WINDOW_GAP, width, height, visualBounds);
    }

    /**
     * Preserves an on-screen position, centers unset coordinates and clamps inaccessible coordinates to the screen.
     */
    static Point2D getWindowPosition(double x, double y, double width, double height, Rectangle2D visualBounds) {
        double visibleWidth = Math.min(width, visualBounds.getWidth());
        double visibleHeight = Math.min(height, visualBounds.getHeight());
        double requestedX = Double.isNaN(x)
                ? visualBounds.getMinX() + (visualBounds.getWidth() - visibleWidth) / 2 : x;
        double requestedY = Double.isNaN(y)
                ? visualBounds.getMinY() + (visualBounds.getHeight() - visibleHeight) / 2 : y;
        return new Point2D(
                Math.max(visualBounds.getMinX(), Math.min(requestedX, visualBounds.getMaxX() - visibleWidth)),
                Math.max(visualBounds.getMinY(), Math.min(requestedY, visualBounds.getMaxY() - visibleHeight)));
    }

    /**
     * Returns a usable window size that fits the screen, using defaults for unset dimensions.
     */
    static Dimension2D getWindowSize(double width, double height, Rectangle2D visualBounds) {
        double requestedWidth = Double.isNaN(width) || width <= 0 ? DEFAULT_WIDTH : width;
        double requestedHeight = Double.isNaN(height) || height <= 0 ? DEFAULT_HEIGHT : height;
        return new Dimension2D(Math.min(Math.max(requestedWidth, MIN_WIDTH), visualBounds.getWidth()),
                Math.min(Math.max(requestedHeight, MIN_HEIGHT), visualBounds.getHeight()));
    }

    /**
     * Returns true if the help window is currently being shown.
     */
    public boolean isShowing() {
        return getRoot().isShowing();
    }

    /**
     * Hides the help window.
     */
    public void hide() {
        getRoot().hide();
    }

    /**
     * Focuses on the help window.
     */
    public void focus() {
        getRoot().requestFocus();
        commandGuideScrollPane.requestFocus();
    }

    /**
     * Closes Help on a plain Escape press without intercepting scrolling, traversal or platform shortcuts.
     */
    static void handleCloseKey(KeyEvent event, Runnable closeWindow) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED && event.getCode() == KeyCode.ESCAPE
                && !event.isShiftDown() && !event.isControlDown() && !event.isAltDown() && !event.isMetaDown()) {
            closeWindow.run();
            event.consume();
        }
    }

    /**
     * Copies the URL to the user guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        copyStatus.setText(copyUserGuideUrl(Clipboard.getSystemClipboard()::setContent));
    }

    /**
     * Copies the guide URL, logs the outcome and returns feedback for the user.
     */
    static String copyUserGuideUrl(Function<ClipboardContent, Boolean> clipboardWriter) {
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        if (clipboardWriter.apply(url)) {
            logger.fine("Copied User Guide URL to clipboard.");
            return "User Guide URL copied.";
        }
        logger.warning("Could not copy the User Guide URL to the clipboard.");
        return "Could not copy the User Guide URL. Try again.";
    }
}
