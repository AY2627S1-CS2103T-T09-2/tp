package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class StatusTest {

    @Test
    public void default_isProspect() {
        assertEquals(Status.PROSPECT, Status.DEFAULT);
    }

    @Test
    public void isValidStatus() {
        // EP: null input, which violates the method's non-null contract.
        assertThrows(NullPointerException.class, () -> Status.isValidStatus(null));

        // EP: input that is empty after trimming.
        assertFalse(Status.isValidStatus(""));
        assertFalse(Status.isValidStatus(" "));

        // EP: names outside the supported statuses, including near-matches.
        assertFalse(Status.isValidStatus("lead"));
        assertFalse(Status.isValidStatus("prospects"));

        // EP: supported status names; representatives cover exact case, different case and surrounding spaces.
        assertTrue(Status.isValidStatus("PROSPECT"));
        assertTrue(Status.isValidStatus("client"));
        assertTrue(Status.isValidStatus(" InAcTiVe "));
    }

    @Test
    public void parse_validStatus_returnsStatus() {
        // EP: supported statuses after case and whitespace normalization; each enum value is represented.
        assertEquals(Status.PROSPECT, Status.parse("PROSPECT"));
        assertEquals(Status.CLIENT, Status.parse("client"));
        assertEquals(Status.INACTIVE, Status.parse(" InAcTiVe "));
    }

    @Test
    public void parse_invalidStatus_throwsIllegalArgumentException() {
        // EP: null input is a contract violation rather than an invalid status name.
        assertThrows(NullPointerException.class, () -> Status.parse(null));

        // EP: empty status name.
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.parse(""));

        // EP: nonempty but unsupported status name.
        assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.parse("lead"));
    }

    @Test
    public void toString_returnsUppercaseStatus() {
        assertEquals("PROSPECT", Status.PROSPECT.toString());
        assertEquals("CLIENT", Status.CLIENT.toString());
        assertEquals("INACTIVE", Status.INACTIVE.toString());
    }
}
