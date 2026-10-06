package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StatusTest {

    @Test
    public void isValidStatus() {
        assertThrows(NullPointerException.class, () -> Status.isValidStatus(null));

        assertFalse(Status.isValidStatus(""));
        assertFalse(Status.isValidStatus(" "));
        assertFalse(Status.isValidStatus("lead"));
        assertFalse(Status.isValidStatus("prospects"));

        assertTrue(Status.isValidStatus("PROSPECT"));
        assertTrue(Status.isValidStatus("client"));
        assertTrue(Status.isValidStatus(" InAcTiVe "));
    }

    @Test
    public void parse_validStatus_returnsStatus() {
        assertEquals(Status.PROSPECT, Status.parse("PROSPECT"));
        assertEquals(Status.CLIENT, Status.parse("client"));
        assertEquals(Status.INACTIVE, Status.parse(" InAcTiVe "));
    }

    @Test
    public void parse_invalidStatus_throwsIllegalArgumentException() {
        assertThrows(NullPointerException.class, () -> Status.parse(null));
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.parse(""));
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.parse("lead"));
    }

    @Test
    public void toString_returnsUppercaseStatus() {
        assertEquals("PROSPECT", Status.PROSPECT.toString());
        assertEquals("CLIENT", Status.CLIENT.toString());
        assertEquals("INACTIVE", Status.INACTIVE.toString());
    }
}
