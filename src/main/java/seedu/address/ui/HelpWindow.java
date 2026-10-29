package seedu.address.ui;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.LogsCenter;

/**
 * Controller for a help page.
 */
public class HelpWindow extends UiPart<Stage> {

    public static final String USERGUIDE_URL =
            "https://ay2627s1-cs2103t-t09-2.github.io/tp/UserGuide.html";

    private static final Logger logger = LogsCenter.getLogger(HelpWindow.class);
    private static final String FXML = "HelpWindow.fxml";
    private static final double DEFAULT_WIDTH = 1040;
    private static final double DEFAULT_HEIGHT = 720;
    private static final double MIN_WIDTH = 600;
    private static final double MIN_HEIGHT = 480;
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
        layoutCommandGuide(false);
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
        fitToScreen();
        copyStatus.setText("");
        getRoot().show();
        logger.fine("Showing command guide.");
    }

    /**
     * Fits and centers the help window within its screen's usable area.
     */
    private void fitToScreen() {
        Stage stage = getRoot();
        double previousWidth = stage.getWidth();
        double previousHeight = stage.getHeight();
        Screen screen = Screen.getPrimary();

        if (!Double.isNaN(stage.getX()) && !Double.isNaN(stage.getY())) {
            screen = Screen.getScreensForRectangle(stage.getX(), stage.getY(), 1, 1).stream()
                    .findFirst().orElse(screen);
        }

        Rectangle2D visualBounds = screen.getVisualBounds();
        Dimension2D size = getWindowSize(previousWidth, previousHeight, visualBounds);
        double width = size.getWidth();
        double height = size.getHeight();
        stage.setMinWidth(Math.min(MIN_WIDTH, visualBounds.getWidth()));
        stage.setMinHeight(Math.min(MIN_HEIGHT, visualBounds.getHeight()));
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(visualBounds.getMinX() + (visualBounds.getWidth() - width) / 2);
        stage.setY(visualBounds.getMinY() + (visualBounds.getHeight() - height) / 2);
        if (Double.compare(previousWidth, width) != 0 || Double.compare(previousHeight, height) != 0) {
            logger.fine("Adjusted command guide size; from=" + previousWidth + "x" + previousHeight
                    + ", to=" + width + "x" + height);
        }
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
