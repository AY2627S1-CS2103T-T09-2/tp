package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    private static final String EXPECTED_DETAILS = "Amy Bee; Phone: 85355255; Email: amy@gmail.com; "
            + "Address: 123, Jurong West Ave 6, #08-111; Status: PROSPECT; ";

    @Test
    public void format_noTagsAndNoFollowUp_showsBothEmptyFields() {
        // EP: both optional fields are empty; neither label should have a blank or missing value.
        Person person = new PersonBuilder().build();

        assertEquals(EXPECTED_DETAILS + "Tags: none; Follow-up: none", Messages.format(person));
    }

    @Test
    public void format_noTagsWithFollowUp_showsEmptyTagsAndDate() {
        // EP: tags are empty while a follow-up is present; retain the date in its original format.
        Person person = new PersonBuilder().withFollowUp("25-12-2099").build();

        assertEquals(EXPECTED_DETAILS + "Tags: none; Follow-up: 25-12-2099", Messages.format(person));
    }

    @Test
    public void format_singleTagWithoutFollowUp_showsTagAndEmptyDate() {
        // Boundary: one tag needs no separator and must not introduce trailing whitespace.
        Person person = new PersonBuilder().withTags("referral").build();

        assertEquals(EXPECTED_DETAILS + "Tags: [referral]; Follow-up: none", Messages.format(person));
    }

    @Test
    public void format_multipleTagsWithFollowUp_separatesTagsInCardOrder() {
        // EP: multiple tags include mixed case, Unicode and a name that prefixes another tag's name.
        // Sort names before adding brackets so that "a" precedes "a0", as on the contact card.
        Person person = new PersonBuilder().withTags("a0", "\u00e9quipe", "a", "Zulu")
                .withFollowUp("25-12-2099").build();

        assertEquals(EXPECTED_DETAILS + "Tags: [Zulu] [a] [a0] [\u00e9quipe]; Follow-up: 25-12-2099",
                Messages.format(person));
    }
}
