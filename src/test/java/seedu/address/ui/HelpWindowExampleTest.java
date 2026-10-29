package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import javafx.scene.text.Font;

/**
 * Tests example sizing without a graphical environment, including wrapping and explicit line breaks.
 */
public class HelpWindowExampleTest {

    private static final Font FONT = Font.font("Monospaced", 14);
    private static final String LONG_EXAMPLE = "add n/Sarah Lim p/98765432 e/sarahlim@example.com "
            + "a/Tampines st/PROSPECT f/25-12-2026 t/referral";

    @Test
    public void getExampleTextHeight_singleLine_hasUsableHeight() {
        // EP: short examples fit on one line at both table and stacked widths.
        double height = HelpWindow.getExampleTextHeight("help", FONT, 250);
        assertTrue(height > 0);
        assertEquals(height, HelpWindow.getExampleTextHeight("help", FONT, 500));
    }

    @Test
    public void getExampleTextHeight_multipleLines_preservesLineBreaks() {
        // EP: clear has two explicit examples; both lines must remain visible even without wrapping.
        double singleLine = HelpWindow.getExampleTextHeight("clear", FONT, 500);
        double multipleLines = HelpWindow.getExampleTextHeight("clear\nclear confirm", FONT, 500);
        assertTrue(multipleLines > singleLine);
        assertEquals(multipleLines, HelpWindow.getExampleTextHeight("clear\nclear confirm", FONT, 250));
    }

    @Test
    public void getExampleTextHeight_wrappedText_growsAndShrinksWithWidth() {
        // EP: long examples wrap in narrow columns, then shrink again when the window becomes wider.
        double narrowHeight = HelpWindow.getExampleTextHeight(LONG_EXAMPLE, FONT, 200);
        double wideHeight = HelpWindow.getExampleTextHeight(LONG_EXAMPLE, FONT, 600);
        assertTrue(narrowHeight > wideHeight);
        assertEquals(narrowHeight, HelpWindow.getExampleTextHeight(LONG_EXAMPLE, FONT, 200));
    }

    @Test
    public void getExampleTextHeight_largerFont_reservesMoreSpace() {
        // EP: a larger effective font requires more vertical space instead of clipping the example.
        double normalHeight = HelpWindow.getExampleTextHeight(LONG_EXAMPLE, FONT, 300);
        double largerHeight = HelpWindow.getExampleTextHeight(LONG_EXAMPLE, Font.font("Monospaced", 28), 300);
        assertTrue(largerHeight > normalHeight);
    }

    @Test
    public void getExampleTextHeight_unavailableWidth_staysFinite() {
        // EP: layout has not assigned a usable width yet; zero and negative widths must remain safe.
        // Boundary value: one pixel is the minimum measurement width.
        double minimumWidthHeight = HelpWindow.getExampleTextHeight("help", FONT, 1);
        assertTrue(Double.isFinite(minimumWidthHeight));
        assertTrue(minimumWidthHeight > 0);
        assertEquals(minimumWidthHeight, HelpWindow.getExampleTextHeight("help", FONT, 0));
        assertEquals(minimumWidthHeight, HelpWindow.getExampleTextHeight("help", FONT, -1));
    }
}
