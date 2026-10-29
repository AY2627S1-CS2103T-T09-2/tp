package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void getInteractionNotes_modifyList_throwsUnsupportedOperationException() {
        // EP: modifying the exposed notes list must not mutate the contact's history.
        Person person = new Person(
                ALICE.getName(),
                ALICE.getPhone(),
                ALICE.getEmail(),
                ALICE.getAddress(),
                ALICE.getStatus(),
                ALICE.getTags(),
                ALICE.getFollowUp(),
                List.of(new InteractionNote("Met client")));

        List<InteractionNote> notes = person.getInteractionNotes();

        assertThrows(UnsupportedOperationException.class, () -> notes.add(
                new InteractionNote("Another note")));
    }

    @Test
    public void isSamePerson() {
        // EP: the same object is always the same contact.
        assertTrue(ALICE.isSamePerson(ALICE));

        // EP: null cannot identify a contact.
        assertFalse(ALICE.isSamePerson(null));

        // EP: the same name with different non-identity fields still identifies the same contact.
        Person editedAlice = new PersonBuilder(ALICE)
                .withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB)
                .withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // EP: a different name identifies a different contact, even when other fields match.
        editedAlice = new PersonBuilder(ALICE)
                .withName(VALID_NAME_BOB)
                .build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // EP: names differing only in case identify the same contact.
        Person editedBob = new PersonBuilder(BOB)
                .withName(VALID_NAME_BOB.toLowerCase())
                .build();
        assertTrue(BOB.isSamePerson(editedBob));

        // EP: names equivalent after whitespace normalization identify the same contact.
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB)
                .withName(nameWithTrailingSpaces)
                .build();
        assertTrue(BOB.isSamePerson(editedBob));

        String nameWithRepeatedSpaces = VALID_NAME_BOB.replace(" ", "   ");
        editedBob = new PersonBuilder(BOB)
                .withName(nameWithRepeatedSpaces)
                .build();
        assertTrue(BOB.isSamePerson(editedBob));
    }

    @Test
    public void isSamePerson_canonicallyEquivalentNames_returnsTrue() {
        // EP: equivalent Unicode spellings identify the same contact and preserve complete value equality.
        Person composed = new PersonBuilder().withName("José Núñez").build();
        Person decomposed = new PersonBuilder(composed).withName("Jose\u0301 Nu\u0301n\u0303ez").build();
        assertTrue(composed.isSamePerson(decomposed));
        assertTrue(decomposed.isSamePerson(composed));
        assertEquals(composed, decomposed);
        assertEquals(composed.hashCode(), decomposed.hashCode());

        // EP: existing case and repeated-space rules still apply to canonical names.
        Person caseAndSpaceVariant = new PersonBuilder(composed)
                .withName("JOSE\u0301  NU\u0301N\u0303EZ ").build();
        assertTrue(composed.isSamePerson(caseAndSpaceVariant));

        // EP: removing an accent changes the name rather than its Unicode representation.
        Person unaccented = new PersonBuilder(composed).withName("Jose Núñez").build();
        assertFalse(composed.isSamePerson(unaccented));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE)
                .withName(VALID_NAME_BOB)
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withPhone(VALID_PHONE_BOB)
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withEmail(VALID_EMAIL_BOB)
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withAddress(VALID_ADDRESS_BOB)
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withTags(VALID_TAG_HUSBAND)
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different status -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withStatus("CLIENT")
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // different follow-up -> returns false
        editedAlice = new PersonBuilder(ALICE)
                .withFollowUp("25-12-2099")
                .build();
        assertFalse(ALICE.equals(editedAlice));

        // EP: contacts with identical other fields but different interaction notes are not equal.
        editedAlice = new Person(
                ALICE.getName(),
                ALICE.getPhone(),
                ALICE.getEmail(),
                ALICE.getAddress(),
                ALICE.getStatus(),
                ALICE.getTags(),
                ALICE.getFollowUp(),
                List.of(new InteractionNote("Met client")));

        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void getFollowUp() {
        // EP: a contact with no pending follow-up.
        assertTrue(ALICE.getFollowUp().isEmpty());

        // EP: a contact with a pending follow-up.
        Person aliceWithFollowUp = new PersonBuilder(ALICE)
                .withFollowUp("25-12-2099")
                .build();

        assertEquals(
                new FollowUp("25-12-2099"),
                aliceWithFollowUp.getFollowUp().get());
    }

    @Test
    public void getInteractionNotes_noNotes_returnsEmptyList() {
        // EP: a contact with no interaction notes has an empty history.
        assertTrue(ALICE.getInteractionNotes().isEmpty());
    }

    @Test
    public void getInteractionNotes_withNotes_returnsNotes() {
        // EP: a contact with recorded interaction notes returns those notes.
        InteractionNote note = new InteractionNote("Discussed hospital coverage");

        Person aliceWithNote = new Person(
                ALICE.getName(),
                ALICE.getPhone(),
                ALICE.getEmail(),
                ALICE.getAddress(),
                ALICE.getStatus(),
                ALICE.getTags(),
                ALICE.getFollowUp(),
                List.of(note));

        assertEquals(
                List.of(note),
                aliceWithNote.getInteractionNotes());
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName()
                + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail()
                + ", address=" + ALICE.getAddress()
                + ", status=" + ALICE.getStatus()
                + ", tags=" + ALICE.getTags()
                + ", followUp=none"
                + ", lastContact=none"
                + ", interactionNotes=[]}";

        assertEquals(expected, ALICE.toString());
    }

    @Test
    public void constructor_withoutStatus_defaultsToProspect() {
        assertEquals(Status.PROSPECT, ALICE.getStatus());
    }
}
