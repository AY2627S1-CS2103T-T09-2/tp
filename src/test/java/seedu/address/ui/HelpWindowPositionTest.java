package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

/**
 * Tests Help window placement without requiring a graphical environment.
 */
public class HelpWindowPositionTest {

    @Test
    public void getInitialPosition_rightSideFits_prefersRightSide() {
        // EP: both sides have space; Help opens to the right with a gap from the main window.
        Rectangle2D screen = new Rectangle2D(0, 0, 2400, 1080);
        Rectangle2D mainWindow = new Rectangle2D(900, 100, 740, 600);
        assertEquals(new Point2D(1656, 116), HelpWindow.getInitialPosition(mainWindow, 640, 540, screen));

        // Boundary value: the right side has exactly enough space for Help and the 16-pixel gap.
        Rectangle2D exactFit = new Rectangle2D(1104, 100, 640, 600);
        assertEquals(new Point2D(1760, 116), HelpWindow.getInitialPosition(exactFit, 640, 540, screen));
    }

    @Test
    public void getInitialPosition_onlyLeftSideFits_usesLeftSide() {
        // EP: the right side is too narrow, but the left side has space for the whole window and gap.
        Rectangle2D screen = new Rectangle2D(0, 0, 2400, 1080);
        // Boundary value: the right side is one pixel short of the required space.
        Rectangle2D mainWindow = new Rectangle2D(1105, 100, 640, 600);
        assertEquals(new Point2D(449, 116), HelpWindow.getInitialPosition(mainWindow, 640, 540, screen));

        // Boundary value: the left side has exactly enough space for Help and the gap.
        Rectangle2D smallerScreen = new Rectangle2D(0, 0, 1920, 1080);
        Rectangle2D exactFit = new Rectangle2D(656, 100, 740, 600);
        assertEquals(new Point2D(0, 116), HelpWindow.getInitialPosition(exactFit, 640, 540, smallerScreen));
    }

    @Test
    public void getInitialPosition_neitherSideFits_usesLessOccupiedEdge() {
        // EP: overlap is unavoidable; use the edge with more free space without moving the main window.
        Rectangle2D screen = new Rectangle2D(0, 0, 1280, 1080);
        assertEquals(new Point2D(640, 116), HelpWindow.getInitialPosition(
                new Rectangle2D(100, 100, 740, 600), 640, 540, screen));
        assertEquals(new Point2D(0, 116), HelpWindow.getInitialPosition(
                new Rectangle2D(440, 100, 740, 600), 640, 540, screen));
        // EP: both edges have equal space; choose the right edge consistently.
        assertEquals(new Point2D(640, 116), HelpWindow.getInitialPosition(
                new Rectangle2D(270, 100, 740, 600), 640, 540, screen));
    }

    @Test
    public void getInitialPosition_nearVerticalEdges_keepsWindowOnScreen() {
        // EP: the main window's vertical offset would place part of Help above or below the usable screen.
        Rectangle2D screen = new Rectangle2D(0, 40, 1920, 1080);
        assertEquals(new Point2D(856, 40), HelpWindow.getInitialPosition(
                new Rectangle2D(100, 0, 740, 600), 640, 540, screen));
        assertEquals(new Point2D(856, 580), HelpWindow.getInitialPosition(
                new Rectangle2D(100, 1000, 740, 600), 640, 540, screen));
    }

    @Test
    public void getWindowPosition_validPosition_preservesPosition() {
        // EP: a user-chosen position is retained when Help is reopened in the same app session.
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        assertEquals(new Point2D(120, 230), HelpWindow.getWindowPosition(120, 230, 640, 540, screen));
        // Boundary values: exactly touching the top-left and bottom-right edges remains valid.
        assertEquals(new Point2D(0, 0), HelpWindow.getWindowPosition(0, 0, 640, 540, screen));
        assertEquals(new Point2D(1280, 540), HelpWindow.getWindowPosition(1280, 540, 640, 540, screen));
        // EP: a resized window keeps its chosen position when its new dimensions still fit.
        assertEquals(new Point2D(100, 150), HelpWindow.getWindowPosition(100, 150, 900, 700, screen));
    }

    @Test
    public void getWindowPosition_outsideScreen_clampsOnlyInaccessibleAxis() {
        // EP: one coordinate is outside the screen while the other remains usable.
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        // Boundary values: one pixel beyond each of the four valid edges.
        assertEquals(new Point2D(0, 100), HelpWindow.getWindowPosition(-1, 100, 640, 540, screen));
        assertEquals(new Point2D(1280, 100), HelpWindow.getWindowPosition(1281, 100, 640, 540, screen));
        assertEquals(new Point2D(100, 0), HelpWindow.getWindowPosition(100, -1, 640, 540, screen));
        assertEquals(new Point2D(100, 540), HelpWindow.getWindowPosition(100, 541, 640, 540, screen));
    }

    @Test
    public void getWindowPosition_unsetCoordinates_centersOnlyUnsetAxis() {
        // EP: a window without a main-window reference has unset coordinates on its first opening.
        Rectangle2D screen = new Rectangle2D(0, 0, 1920, 1080);
        assertEquals(new Point2D(640, 270), HelpWindow.getWindowPosition(Double.NaN, Double.NaN, 640, 540, screen));
        // EP: only one coordinate is unset; preserve the other user-chosen coordinate.
        assertEquals(new Point2D(640, 100), HelpWindow.getWindowPosition(Double.NaN, 100, 640, 540, screen));
        assertEquals(new Point2D(100, 270), HelpWindow.getWindowPosition(100, Double.NaN, 640, 540, screen));
    }

    @Test
    public void getWindowPosition_secondaryScreen_usesItsActualOrigin() {
        // EP: a screen to the left of the primary screen has valid negative coordinates.
        Rectangle2D screen = new Rectangle2D(-1920, 40, 1920, 1080);
        assertEquals(new Point2D(-1500, 200), HelpWindow.getWindowPosition(-1500, 200, 640, 540, screen));
        assertEquals(new Point2D(-1920, 40), HelpWindow.getWindowPosition(-2000, 0, 640, 540, screen));
        assertEquals(new Point2D(-640, 580), HelpWindow.getWindowPosition(0, 1200, 640, 540, screen));
        assertEquals(new Point2D(-1280, 310), HelpWindow.getWindowPosition(Double.NaN, Double.NaN, 640, 540, screen));
        assertEquals(new Point2D(-1144, 116), HelpWindow.getInitialPosition(
                new Rectangle2D(-1900, 100, 740, 600), 640, 540, screen));
    }

    @Test
    public void getWindowPosition_disconnectedScreen_recoversWithinFallbackScreen() {
        // EP: remembered coordinates belong to a disconnected monitor; fit them into the fallback screen.
        Rectangle2D screen = new Rectangle2D(0, 40, 1920, 1080);
        assertEquals(new Point2D(0, 100), HelpWindow.getWindowPosition(-1800, 100, 640, 540, screen));
        assertEquals(new Point2D(1280, 580), HelpWindow.getWindowPosition(2500, 1300, 640, 540, screen));
    }

    @Test
    public void getWindowPosition_smallScreen_anchorsOversizedWindowWithinScreen() {
        // EP: fitting an oversized window fills the available screen, so its position must be the screen origin.
        Rectangle2D screen = new Rectangle2D(100, 40, 500, 400);
        assertEquals(new Point2D(100, 40), HelpWindow.getWindowPosition(200, 100, 640, 540, screen));
        assertEquals(new Point2D(100, 40), HelpWindow.getWindowPosition(Double.NaN, Double.NaN, 640, 540, screen));
        assertEquals(new Point2D(100, 40), HelpWindow.getInitialPosition(
                new Rectangle2D(100, 40, 450, 600), 640, 540, screen));
    }
}
