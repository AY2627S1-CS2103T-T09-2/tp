package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_REQUIRED_FIELDS;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.FOLLOW_UP_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.FOLLOW_UP_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_FOLLOW_UP_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_STATUS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.STATUS_DESC_CLIENT;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_FOLLOW_UP_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_statusOmitted_usesDefault() {
        // EP: optional status omitted; the parser supplies the default status.
        Person expectedPerson = new PersonBuilder(AMY).withTags().withStatus(Status.DEFAULT.name()).build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_statusProvided_setsStatus() {
        // EP: one supported status; uppercase and lowercase values identify the same status.
        Person expectedPerson = new PersonBuilder(AMY).withTags().withStatus("CLIENT").build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + STATUS_DESC_CLIENT, new AddCommand(expectedPerson));
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + " " + PREFIX_STATUS + "client", new AddCommand(expectedPerson));
    }

    @Test
    public void parse_invalidStatus_throwsParseException() {
        // EP: a supplied status outside the supported set.
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + INVALID_STATUS_DESC, Status.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedStatus_throwsParseException() {
        // EP: more than one status prefix; two identical occurrences still violate the maximum of one.
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + STATUS_DESC_CLIENT + STATUS_DESC_CLIENT,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STATUS));
    }

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND).build();

        // EP: whitespace-only preamble is ignored when all required fields are present.
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND, new AddCommand(expectedPerson));


        // EP: repeated tag prefixes are allowed and retain all distinct tags.
        Person expectedPersonMultipleTags = new PersonBuilder(BOB).withTags(VALID_TAG_FRIEND, VALID_TAG_HUSBAND)
                .build();
        assertParseSuccess(parser,
                NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                new AddCommand(expectedPersonMultipleTags));
    }

    @Test
    public void parse_repeatedNonTagValue_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_FRIEND;

        // EP: repeated name prefix, with otherwise valid values.
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // EP: repeated phone prefix, with otherwise valid values.
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // EP: repeated email prefix, with otherwise valid values.
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // EP: repeated address prefix, with otherwise valid values.
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // EP: several single-valued fields repeated; report every duplicated prefix.
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE));

        // EP: an invalid occurrence before a valid one still counts as a duplicate prefix.

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // EP: a valid occurrence before an invalid one still counts as a duplicate prefix.

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_optionalFieldsMissing_success() {
        // EP: optional fields omitted; zero tags is the lower boundary of the tag count.
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_followUpPresent_success() {
        // EP: one optional follow-up containing a real date in the required format.
        Person expectedPerson = new PersonBuilder(AMY).withTags().withFollowUp(VALID_FOLLOW_UP_AMY).build();
        assertParseSuccess(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + FOLLOW_UP_DESC_AMY, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_impossibleFollowUpDate_failure() {
        // EP: the date has the required format but is not a real calendar date.
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + INVALID_FOLLOW_UP_DESC, String.format(FollowUp.MESSAGE_NOT_A_REAL_DATE, "31-02-2099"));
    }

    @Test
    public void parse_wrongFollowUpFormat_failure() {
        // EP: the date exists but is written in the wrong format.
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + " " + PREFIX_FOLLOW_UP + "2099-12-25", FollowUp.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_blankFollowUp_failure() {
        // EP: f/ is given without a date; on add this is rejected rather than read as "no follow-up".
        String requiredFields = NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        assertParseFailure(parser, requiredFields + " " + PREFIX_FOLLOW_UP,
                AddCommandParser.MESSAGE_BLANK_FOLLOW_UP);
        assertParseFailure(parser, requiredFields + " " + PREFIX_FOLLOW_UP + "   ",
                AddCommandParser.MESSAGE_BLANK_FOLLOW_UP);
        // A blank f/ followed by another prefix is still blank.
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + " " + PREFIX_FOLLOW_UP
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY, AddCommandParser.MESSAGE_BLANK_FOLLOW_UP);
    }

    @Test
    public void parse_repeatedFollowUp_failure() {
        // EP: more than one follow-up prefix; two is the first invalid occurrence count.
        assertParseFailure(parser, NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY + ADDRESS_DESC_AMY
                + FOLLOW_UP_DESC_AMY + FOLLOW_UP_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FOLLOW_UP));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        // EP: one required prefix absent; each required field is named on its own.
        assertParseFailure(parser, PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("n/NAME"));
        assertParseFailure(parser, NAME_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("p/PHONE"));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("e/EMAIL"));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                missingFieldsMessage("a/ADDRESS"));

        // EP: required prefix absent, with its bare value swallowed by the previous field.
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("p/PHONE"));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("e/EMAIL"));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB,
                missingFieldsMessage("a/ADDRESS"));
    }

    @Test
    public void parse_missingFieldMessage_namesFieldAndShowsUsage() {
        // EP: the exact wording, so a change to the message is a deliberate one.
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                "Missing required field(s): a/ADDRESS\n" + AddCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_severalCompulsoryFieldsMissing_failure() {
        // EP: two or three required prefixes absent; all of them are listed in command format order.
        assertParseFailure(parser, PHONE_DESC_BOB + EMAIL_DESC_BOB,
                missingFieldsMessage("n/NAME, a/ADDRESS"));
        assertParseFailure(parser, NAME_DESC_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("p/PHONE, e/EMAIL"));
        assertParseFailure(parser, ADDRESS_DESC_BOB,
                missingFieldsMessage("n/NAME, p/PHONE, e/EMAIL"));

        // EP: input order does not change the order of the listed fields.
        assertParseFailure(parser, ADDRESS_DESC_BOB + NAME_DESC_BOB,
                missingFieldsMessage("p/PHONE, e/EMAIL"));
        assertParseFailure(parser, EMAIL_DESC_BOB + TAG_DESC_FRIEND + PHONE_DESC_BOB,
                missingFieldsMessage("n/NAME, a/ADDRESS"));
    }

    @Test
    public void parse_allCompulsoryFieldsMissing_failure() {
        String allMissing = missingFieldsMessage("n/NAME, p/PHONE, e/EMAIL, a/ADDRESS");

        // EP: no arguments at all.
        assertParseFailure(parser, "", allMissing);
        assertParseFailure(parser, PREAMBLE_WHITESPACE, allMissing);

        // EP: only optional fields given.
        assertParseFailure(parser, STATUS_DESC_CLIENT + FOLLOW_UP_DESC_AMY + TAG_DESC_FRIEND, allMissing);
    }

    @Test
    public void parse_preambleWithoutNamePrefix_reportsMissingName() {
        // EP: the name is typed without n/, the most likely way to leave it out, so n/NAME is reported
        // like any other missing field.
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                missingFieldsMessage("n/NAME"));
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB, missingFieldsMessage("n/NAME, e/EMAIL, a/ADDRESS"));

        // EP: all required prefixes absent; every field is reported.
        assertParseFailure(parser, VALID_NAME_BOB + " " + VALID_PHONE_BOB + " " + VALID_EMAIL_BOB + " "
                + VALID_ADDRESS_BOB, missingFieldsMessage("n/NAME, p/PHONE, e/EMAIL, a/ADDRESS"));
    }

    @Test
    public void parse_preambleWithNamePrefix_showsUsage() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // EP: n/ is given, so the text before it cannot be a forgotten name; the format is wrong.
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB, expectedMessage);
        assertParseFailure(parser, VALID_NAME_BOB + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB, expectedMessage);
    }

    @Test
    public void parse_compulsoryFieldMissingAndRepeatedField_reportsMissingField() {
        // EP: a missing required field is reported before a repeated one.
        assertParseFailure(parser, NAME_DESC_AMY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB,
                missingFieldsMessage("a/ADDRESS"));
    }

    @Test
    public void parse_prefixLikeTextInsideValue_notTreatedAsPrefix() {
        // EP: "a/" inside a word is not a prefix, because prefixes must follow whitespace.
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " " + PREFIX_EMAIL + "data/bob@example.com",
                missingFieldsMessage("a/ADDRESS"));
        // EP: an uppercase prefix is not a prefix.
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " A/1 Road",
                missingFieldsMessage("a/ADDRESS"));
    }

    @Test
    public void parse_invalidValue_failure() {
        // EP: name contains unsupported punctuation.
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Name.MESSAGE_CONSTRAINTS);

        // EP: phone number contains a letter.
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Phone.MESSAGE_CONSTRAINTS);

        // EP: email address lacks the @ separator.
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Email.MESSAGE_CONSTRAINTS);

        // EP: empty required address; zero characters is below the accepted minimum.
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + TAG_DESC_HUSBAND + TAG_DESC_FRIEND, Address.MESSAGE_CONSTRAINTS);

        // EP: a tag contains unsupported punctuation.
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + INVALID_TAG_DESC + VALID_TAG_FRIEND, Tag.MESSAGE_CONSTRAINTS);

        // EP: multiple invalid field values; the first validation error is reported.
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC,
                Name.MESSAGE_CONSTRAINTS);

        // EP: nonblank text before the first prefix is unsupported.
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    private static String missingFieldsMessage(String missingFields) {
        return String.format(MESSAGE_MISSING_REQUIRED_FIELDS, missingFields, AddCommand.MESSAGE_USAGE);
    }
}
