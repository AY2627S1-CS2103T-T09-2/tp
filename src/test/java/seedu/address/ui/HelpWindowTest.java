package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelpWindowTest {

    @Test
    public void userGuideUrl_pointsToEasyInsuranceUserGuide() {
        String expectedUrl =
                "https://ay2627s1-cs2103t-t09-2.github.io/tp/UserGuide.html";

        assertEquals(expectedUrl, HelpWindow.USERGUIDE_URL);
    }
}
