package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class StatusTest {

    private static final List<String> INVALID_UNICODE_STATUSES =
            List.of("CL\u0130ENT", "cl\u0131ent", "PRO\u017fPECT", "\uff23LIENT", "\u0421LIENT");

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
    public void isValidStatus_unicodeLookalikes_returnsFalse() {
        // EP: non-ASCII lookalikes: dotted I, dotless i, long s, full-width C and Cyrillic C.
        for (String input : INVALID_UNICODE_STATUSES) {
            assertFalse(Status.isValidStatus(input), input);
        }
    }

    @Test
    public void parse_unicodeLookalikes_throwsIllegalArgumentException() {
        // EP: Unicode lookalikes must receive the same constraint message as unsupported ASCII names.
        for (String input : INVALID_UNICODE_STATUSES) {
            assertThrows(IllegalArgumentException.class, Status.MESSAGE_CONSTRAINTS, () -> Status.parse(input));
        }
    }

    @Test
    public void parse_asciiStatusesUnderTurkishLocale_returnsStatus() {
        Locale originalLocale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            // EP: each ASCII status accepts mixed case and surrounding whitespace in any default locale.
            assertTrue(Status.isValidStatus(" pRoSpEcT "));
            assertEquals(Status.PROSPECT, Status.parse(" pRoSpEcT "));
            assertTrue(Status.isValidStatus("\tcLiEnT\r\n"));
            assertEquals(Status.CLIENT, Status.parse("\tcLiEnT\r\n"));
            assertTrue(Status.isValidStatus(" InAcTiVe "));
            assertEquals(Status.INACTIVE, Status.parse(" InAcTiVe "));
        } finally {
            Locale.setDefault(originalLocale);
        }
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
