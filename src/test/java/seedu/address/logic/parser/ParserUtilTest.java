package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LAST_CONTACT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+65+1234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseStatus_validInput_returnsStatus() throws Exception {
        // EP: all supported ASCII statuses accept different case and surrounding whitespace.
        assertEquals(Status.PROSPECT, ParserUtil.parseStatus(" pRoSpEcT "));
        assertEquals(Status.CLIENT, ParserUtil.parseStatus(" client "));
        assertEquals(Status.INACTIVE, ParserUtil.parseStatus("\tInAcTiVe\r\n"));
    }

    @Test
    public void parseStatus_invalidInput_throwsParseException() {
        // EP: a nonempty status name outside the supported set.
        assertThrows(ParseException.class, Status.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStatus("lead"));
    }

    @Test
    public void parseStatus_unicodeLookalikes_throwsParseException() {
        // EP: dotted I, dotless i and long s resemble ASCII letters but are not valid status spellings.
        for (String input : List.of("CL\u0130ENT", "cl\u0131ent", "PRO\u017fPECT")) {
            assertThrows(ParseException.class, Status.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseStatus(input));
        }
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        // EP: extra text prevents the whole input from being an unsigned integer.
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        // EP: integer overflow; the value immediately above Integer.MAX_VALUE is the boundary.
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
                -> ParserUtil.parseIndex(Long.toString((long) Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // EP: one unsigned positive integer; 1 is the minimum accepted index (boundary value).
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // EP: the same integer surrounded by whitespace, which is trimmed.
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        // EP: null names violate the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        // EP: names containing unsupported punctuation (here, @).
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        // EP: valid names without surrounding whitespace.
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        // EP: valid names surrounded by whitespace that must be removed.
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        // EP: null phone numbers violate the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        // EP: phone numbers containing a plus sign after the start.
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        // EP: valid phone numbers without surrounding whitespace.
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        // EP: valid phone numbers surrounded by whitespace that must be removed.
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseFollowUp_null_throwsNullPointerException() {
        // EP: null dates violate the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parseFollowUp((String) null));
    }

    @Test
    public void parseFollowUp_invalidValue_throwsParseException() {
        // EP: correct date format but an impossible calendar date.
        assertThrows(ParseException.class, () -> ParserUtil.parseFollowUp("31-02-2026"));

        // EP: a real date written in a format other than DD-MM-YYYY.
        assertThrows(ParseException.class, () -> ParserUtil.parseFollowUp("2026-12-25"));
    }

    @Test
    public void parseFollowUp_validValueWithWhitespace_returnsTrimmedFollowUp() throws Exception {
        // EP: real dates in DD-MM-YYYY format with removable surrounding whitespace.
        FollowUp expectedFollowUp = new FollowUp("25-12-2026");
        assertEquals(expectedFollowUp, ParserUtil.parseFollowUp(WHITESPACE + "25-12-2026" + WHITESPACE));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        // EP: null addresses violate the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        // EP: addresses that become empty after trimming.
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        // EP: nonempty addresses without surrounding whitespace.
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        // EP: nonempty addresses surrounded by whitespace that must be removed.
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        // EP: null email addresses violate the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        // EP: email addresses missing the @ separator.
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        // EP: valid email addresses without surrounding whitespace.
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        // EP: valid email addresses surrounded by whitespace that must be removed.
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        // EP: null tags violate the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        // EP: tags containing a non-alphanumeric character (here, #).
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        // EP: alphanumeric tags without surrounding whitespace.
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        // EP: alphanumeric tags surrounded by whitespace that must be removed.
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        // EP: a null collection violates the parser precondition.
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        // EP: a collection containing at least one invalid tag is rejected as a whole.
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        // EP: an empty collection; zero tags is the accepted lower boundary.
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        // EP: a nonempty collection in which every tag is valid.
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void findUnsupportedPrefix_noOtherAppPrefix_returnsEmpty() {
        // EP: only supported prefixes are used.
        assertEquals(Optional.empty(),
                ParserUtil.findUnsupportedPrefix(" n/Bob p/91234567", PREFIX_NAME, PREFIX_PHONE));

        // EP: slashes that are not app prefixes, inside a word, or in the name forms a/l, a/p and d/o.
        assertEquals(Optional.empty(), ParserUtil.findUnsupportedPrefix(
                " n/Ravi s/o Kumar x/abc data/nt/x", PREFIX_NAME));
        assertEquals(Optional.empty(), ParserUtil.findUnsupportedPrefix(
                " n/Siti d/o Ahmad Devi a/p Raju Ravi A/L Kumar Lim D/O", PREFIX_NAME));
        assertEquals(Optional.empty(), ParserUtil.findUnsupportedPrefix(" n/Siti d/o-Kumar a/l.", PREFIX_NAME));

        // EP: prefixes are lowercase, so a capitalised one is not a prefix.
        assertEquals(Optional.empty(), ParserUtil.findUnsupportedPrefix(" n/Bob NT/x", PREFIX_NAME));
    }

    @Test
    public void findUnsupportedPrefix_otherAppPrefix_returnsFirstOne() {
        // EP: an app prefix the command does not take, after a space, after a tab, or at the start.
        assertEquals(Optional.of(PREFIX_NOTE), ParserUtil.findUnsupportedPrefix(
                " n/Bob nt/Met at roadshow d/27-10-2026", PREFIX_NAME));
        assertEquals(Optional.of(PREFIX_LAST_CONTACT), ParserUtil.findUnsupportedPrefix(
                " n/Bob\td/27-10-2026", PREFIX_NAME));
        assertEquals(Optional.of(PREFIX_ADDRESS), ParserUtil.findUnsupportedPrefix("a/1 Road", PREFIX_NAME));

        // EP: d/ followed by more than the name form d/o is still a prefix.
        assertEquals(Optional.of(PREFIX_LAST_CONTACT), ParserUtil.findUnsupportedPrefix(
                " n/Bob d/old", PREFIX_NAME));
        assertEquals(Optional.of(PREFIX_LAST_CONTACT), ParserUtil.findUnsupportedPrefix(
                " n/Bob d/o1", PREFIX_NAME));
    }
}
