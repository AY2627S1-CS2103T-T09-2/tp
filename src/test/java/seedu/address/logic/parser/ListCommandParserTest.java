package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;
import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.LogCapture;

/**
 * Tests parsing of optional list filters.
 */
public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        // EP: null arguments violate the parser precondition.
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsListCommand() {
        // EP: no filters; empty input and whitespace-only variants show all contacts.
        try (LogCapture logs = new LogCapture(ListCommandParser.class)) {
            assertParseSuccess(parser, "", new ListCommand());
            assertParseSuccess(parser, "   ", new ListCommand());
            assertParseSuccess(parser, " \t\r\n ", new ListCommand());
            assertTrue(logs.getRecords().isEmpty());
        }
    }

    @Test
    public void parse_statusFilter_returnsFilteredCommand() {
        // EP: exactly one supported status; case and surrounding whitespace do not change its meaning.
        ListCommand expectedCommand = new ListCommand(Optional.of(Status.CLIENT), Optional.empty());

        assertParseSuccess(parser, "st/CLIENT", expectedCommand);
        assertParseSuccess(parser, " st/client ", expectedCommand);
        assertParseSuccess(parser, "st/ cLiEnT", expectedCommand);
        assertParseSuccess(parser, "st/PROSPECT",
                new ListCommand(Optional.of(Status.PROSPECT), Optional.empty()));
        assertParseSuccess(parser, "st/INACTIVE",
                new ListCommand(Optional.of(Status.INACTIVE), Optional.empty()));
    }

    @Test
    public void parse_tagFilter_returnsFilteredCommand() {
        // EP: exactly one valid tag; case and surrounding whitespace are normalized.
        ListCommand expectedCommand = new ListCommand(Optional.empty(), Optional.of(new Tag("referral")));

        assertParseSuccess(parser, "t/referral", expectedCommand);
        assertParseSuccess(parser, " t/REFERRAL ", expectedCommand);
    }

    @Test
    public void parse_bothFilters_acceptsEitherOrderAndWhitespace() {
        // EP: one status and one tag; prefix order and separating whitespace do not change either filter.
        ListCommand expectedCommand =
                new ListCommand(Optional.of(Status.CLIENT), Optional.of(new Tag("referral")));

        assertParseSuccess(parser, "st/CLIENT t/referral", expectedCommand);
        assertParseSuccess(parser, "t/REFERRAL st/client", expectedCommand);
        assertParseSuccess(parser, "\tst/client\tt/ReFeRRaL\t", expectedCommand);
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        // EP: a status prefix with no value.
        assertLoggedParseFailure("st/", Status.MESSAGE_CONSTRAINTS, "invalid status filter");

        // EP: a nonempty value outside the supported statuses.
        assertLoggedParseFailure("st/LEAD", Status.MESSAGE_CONSTRAINTS, "invalid status filter");

        // EP: extra text becomes part of the status value, including unknown prefixes.
        assertLoggedParseFailure("st/CLIENT extra", Status.MESSAGE_CONSTRAINTS, "invalid status filter");
        assertLoggedParseFailure("st/CLIENT x/unknown", Status.MESSAGE_CONSTRAINTS, "invalid status filter");
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        // EP: a tag prefix with no value.
        assertLoggedParseFailure("t/", Tag.MESSAGE_CONSTRAINTS, "invalid tag filter");

        // EP: a tag value containing unsupported punctuation.
        assertLoggedParseFailure("t/referral!", Tag.MESSAGE_CONSTRAINTS, "invalid tag filter");

        // EP: multiple words in one tag value, including text from an unknown prefix.
        assertLoggedParseFailure("t/referral extra", Tag.MESSAGE_CONSTRAINTS, "invalid tag filter");
        assertLoggedParseFailure("t/referral x/unknown", Tag.MESSAGE_CONSTRAINTS, "invalid tag filter");
    }

    @Test
    public void parse_unsupportedArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        // EP: a nonempty preamble is unsupported, even when valid filters follow it.
        String[] invalidArguments = {
            "3", "extra", "3 extra", // Bare numeric or textual arguments.
            "p/9123", // A prefix that is not a supported list filter.
            "extra st/CLIENT" // Unrecognized text before an otherwise valid filter.
        };

        for (String arguments : invalidArguments) {
            assertLoggedParseFailure(arguments, expectedMessage, "unsupported preamble");
        }
    }

    @Test
    public void parse_duplicateStatus_throwsParseException() {
        // EP: more than one status prefix; two is the first invalid count, with equal or different values.
        String expectedMessage = MESSAGE_DUPLICATE_FIELDS + "st/";

        assertLoggedParseFailure("st/CLIENT st/PROSPECT", expectedMessage, "duplicate filter prefix");
        assertLoggedParseFailure("st/CLIENT st/CLIENT", expectedMessage, "duplicate filter prefix");
    }

    @Test
    public void parse_duplicateTag_throwsParseException() {
        // EP: more than one tag prefix; two is the first invalid count, with equal or different values.
        String expectedMessage = MESSAGE_DUPLICATE_FIELDS + "t/";

        assertLoggedParseFailure("t/referral t/family", expectedMessage, "duplicate filter prefix");
        assertLoggedParseFailure("t/referral t/referral", expectedMessage, "duplicate filter prefix");
    }

    @Test
    public void parse_contactDetailsInFilter_doesNotLogRawInput() {
        // EP: an invalid tag containing contact details; rejection must not log those details.
        try (LogCapture logs = new LogCapture(ListCommandParser.class)) {
            assertParseFailure(parser, "t/johntan@example.com", Tag.MESSAGE_CONSTRAINTS);
            logs.assertSingleLog(Level.FINE, "invalid tag filter");
            assertFalse(logs.getRecords().getFirst().getMessage().contains("johntan@example.com"));
        }
    }

    private void assertLoggedParseFailure(String args, String expectedMessage, String reason) {
        try (LogCapture logs = new LogCapture(ListCommandParser.class)) {
            assertParseFailure(parser, args, expectedMessage);
            logs.assertSingleLog(Level.FINE, reason);
        }
    }
}
