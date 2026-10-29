package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;

/**
 * Tests Help window size limits without requiring a graphical environment.
 */
public class HelpWindowSizeTest {

    @Test
    public void getWindowSize_usableDimensions_preservesSize() {
        // EP: dimensions inside the usable range are preserved instead of forcing the default size.
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        assertEquals(new Dimension2D(900, 650), HelpWindow.getWindowSize(900, 650, screen));
        // Boundary values: exactly the minimum and exactly the screen's available size.
        assertEquals(new Dimension2D(600, 480), HelpWindow.getWindowSize(600, 480, screen));
        assertEquals(new Dimension2D(1920, 1080), HelpWindow.getWindowSize(1920, 1080, screen));
    }

    @Test
    public void getWindowSize_unsetDimensions_usesDefaults() {
        // EP: unset, zero and negative dimensions use the preferred opening size.
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        assertEquals(new Dimension2D(640, 540), HelpWindow.getWindowSize(Double.NaN, Double.NaN, screen));
        assertEquals(new Dimension2D(640, 540), HelpWindow.getWindowSize(0, -1, screen));
        // EP: each dimension is handled independently; an existing valid height is retained.
        assertEquals(new Dimension2D(640, 600), HelpWindow.getWindowSize(Double.NaN, 600, screen));
        assertEquals(new Dimension2D(900, 540), HelpWindow.getWindowSize(900, 0, screen));
    }

    @Test
    public void getWindowSize_outsideUsableRange_clampsDimensions() {
        // EP: a previously tiny or oversized window must be brought into the usable range.
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        // Boundary values: one pixel below the minimum and one pixel above the screen's limit.
        assertEquals(new Dimension2D(600, 480), HelpWindow.getWindowSize(599, 479, screen));
        assertEquals(new Dimension2D(1920, 1080), HelpWindow.getWindowSize(1921, 1081, screen));
        assertEquals(new Dimension2D(600, 1080), HelpWindow.getWindowSize(100, 2000, screen));
    }

    @Test
    public void getWindowSize_smallScreen_doesNotExceedAvailableSpace() {
        // EP: the screen is smaller than the nominal minimum; fitting the available space takes priority.
        // A secondary screen's origin does not change the available dimensions.
        Rectangle2D screen = new Rectangle2D(-500, 100, 500, 400);
        assertEquals(new Dimension2D(500, 400), HelpWindow.getWindowSize(Double.NaN, Double.NaN, screen));
        assertEquals(new Dimension2D(500, 400), HelpWindow.getWindowSize(100, 100, screen));
        assertEquals(new Dimension2D(500, 400), HelpWindow.getWindowSize(2000, 2000, screen));
    }
}
