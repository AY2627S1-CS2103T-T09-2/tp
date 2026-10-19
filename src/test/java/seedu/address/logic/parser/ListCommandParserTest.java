package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_DUPLICATE_FIELDS;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListCommand;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Tests parsing of optional list filters.
 */
public class ListCommandParserTest {

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_blankArgs_returnsListCommand() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
        assertParseSuccess(parser, " \t\r\n ", new ListCommand());
    }

    @Test
    public void parse_statusFilter_returnsFilteredCommand() {
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
        ListCommand expectedCommand = new ListCommand(Optional.empty(), Optional.of(new Tag("referral")));

        assertParseSuccess(parser, "t/referral", expectedCommand);
        assertParseSuccess(parser, " t/REFERRAL ", expectedCommand);
    }

    @Test
    public void parse_bothFilters_acceptsEitherOrderAndWhitespace() {
        ListCommand expectedCommand =
                new ListCommand(Optional.of(Status.CLIENT), Optional.of(new Tag("referral")));

        assertParseSuccess(parser, "st/CLIENT t/referral", expectedCommand);
        assertParseSuccess(parser, "t/REFERRAL st/client", expectedCommand);
        assertParseSuccess(parser, "\tst/client\tt/ReFeRRaL\t", expectedCommand);
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        assertParseFailure(parser, "st/", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "st/LEAD", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "st/CLIENT extra", Status.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "st/CLIENT x/unknown", Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, "t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/referral!", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/referral extra", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "t/referral x/unknown", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedArguments_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
        String[] invalidArguments = {"3", "extra", "3 extra", "p/9123", "extra st/CLIENT"};

        for (String arguments : invalidArguments) {
            assertParseFailure(parser, arguments, expectedMessage);
        }
    }

    @Test
    public void parse_duplicateStatus_throwsParseException() {
        String expectedMessage = MESSAGE_DUPLICATE_FIELDS + "st/";

        assertParseFailure(parser, "st/CLIENT st/PROSPECT", expectedMessage);
        assertParseFailure(parser, "st/CLIENT st/CLIENT", expectedMessage);
    }

    @Test
    public void parse_duplicateTag_throwsParseException() {
        String expectedMessage = MESSAGE_DUPLICATE_FIELDS + "t/";

        assertParseFailure(parser, "t/referral t/family", expectedMessage);
        assertParseFailure(parser, "t/referral t/referral", expectedMessage);
    }
}
