package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.person.InteractionNote;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.testutil.PersonBuilder;

public class AddressBookTest {

    private final AddressBook addressBook = new AddressBook();

    @Test
    public void constructor() {
        assertEquals(List.of(), addressBook.getPersonList());
    }

    @Test
    public void constructor_nullSource_throwsNullPointerException() {
        // EP: no source address book is supplied.
        assertThrows(NullPointerException.class, () -> new AddressBook(null));
    }

    @Test
    public void constructor_validAddressBook_copiesContactsInOrder() {
        // EP: empty, single-contact and multi-contact snapshots; zero and one are size boundaries.
        for (List<Person> persons : List.of(List.<Person>of(), List.of(ALICE), List.of(BOB, ALICE))) {
            AddressBook original = new AddressBook();
            original.setPersons(persons);

            AddressBook copy = new AddressBook(original);

            assertEquals(original, copy);
            assertEquals(persons, copy.getPersonList());
            assertNotSame(original.getPersonList(), copy.getPersonList());
            assertThrows(UnsupportedOperationException.class, () -> copy.getPersonList().add(CARL));
        }
    }

    @Test
    public void constructor_sourceChanges_keepsSnapshotData() {
        // EP: replacing, removing and adding contacts after a snapshot must not modify that snapshot.
        InteractionNote note = new InteractionNote("Discussed renewal", LocalDateTime.of(2026, 1, 1, 9, 0));
        Person originalAlice = new PersonBuilder(ALICE).withTags("VIP").withFollowUp("25-12-2099")
                .withInteractionNotes(note).build();
        AddressBook original = new AddressBook();
        original.setPersons(List.of(originalAlice, BOB));
        AddressBook copy = new AddressBook(original);

        original.setPerson(originalAlice, new PersonBuilder(originalAlice).withPhone("98765432")
                .withTags("new").withInteractionNotes("Changed note").build());
        original.removePerson(BOB);
        original.addPerson(CARL);

        assertEquals(List.of(originalAlice, BOB), copy.getPersonList());
        // Immutable contacts may be shared; their original tag spelling, follow-up and note timestamp stay intact.
        assertSame(originalAlice, copy.getPersonList().get(0));
        assertEquals("VIP", copy.getPersonList().get(0).getTags().iterator().next().tagName);
        assertEquals(List.of(note), copy.getPersonList().get(0).getInteractionNotes());
    }

    @Test
    public void constructor_copyChanges_keepsSourceData() {
        // EP: editing the copy must not modify the source's live list or its contacts.
        AddressBook original = new AddressBook();
        original.setPersons(List.of(ALICE, BOB));
        ObservableList<Person> originalView = original.getPersonList();
        AddressBook copy = new AddressBook(original);

        copy.setPerson(ALICE, new PersonBuilder(ALICE).withPhone("98765432").build());
        copy.removePerson(BOB);
        copy.addPerson(CARL);

        assertSame(originalView, original.getPersonList());
        assertEquals(List.of(ALICE, BOB), originalView);
    }

    @Test
    public void constructor_readOnlySourceWithDuplicates_throwsDuplicatePersonException() {
        // EP: an external read-only implementation supplies duplicate identities with different contact details.
        Person editedAlice = new PersonBuilder(ALICE).withPhone("98765432").build();
        ReadOnlyAddressBook source = new AddressBookStub(List.of(ALICE, editedAlice));

        assertThrows(DuplicatePersonException.class, () -> new AddressBook(source));
    }

    @Test
    public void constructor_readOnlySource_copiesVisibleContacts() {
        // EP: valid external data is copied independently of its mutable source list.
        AddressBookStub source = new AddressBookStub(List.of(BOB, ALICE));

        AddressBook copy = new AddressBook(source);
        source.getPersonList().clear();

        assertEquals(List.of(BOB, ALICE), copy.getPersonList());
    }

    @Test
    public void constructor_subclassWithOverriddenView_usesVisibleContacts() {
        // EP: a subclass's visible list differs from its inherited internal list.
        AddressBook source = new AddressBook() {
            @Override
            public ObservableList<Person> getPersonList() {
                return FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(BOB, ALICE));
            }
        };

        assertEquals(List.of(BOB, ALICE), new AddressBook(source).getPersonList());
    }

    @Test
    public void constructor_subclassWithDuplicates_throwsDuplicatePersonException() {
        // EP: subclassing must not let an overridden list bypass duplicate validation.
        AddressBook source = new AddressBook() {
            @Override
            public ObservableList<Person> getPersonList() {
                return FXCollections.observableArrayList(ALICE, ALICE);
            }
        };

        assertThrows(DuplicatePersonException.class, () -> new AddressBook(source));
    }

    @Test
    public void resetData_null_throwsNullPointerException() {
        // EP: the replacement address book is null.
        assertThrows(NullPointerException.class, () -> addressBook.resetData(null));
    }

    @Test
    public void resetData_withValidReadOnlyAddressBook_replacesData() {
        // EP: replacement data contains distinct contact identities.
        AddressBook newData = getTypicalAddressBook();
        addressBook.resetData(newData);
        assertEquals(newData, addressBook);
    }

    @Test
    public void resetData_withDuplicatePersons_throwsDuplicatePersonException() {
        // EP: duplicate identities, even when addresses and tags differ.
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        List<Person> newPersons = List.of(ALICE, editedAlice);
        AddressBookStub newData = new AddressBookStub(newPersons);

        assertThrows(DuplicatePersonException.class, () -> addressBook.resetData(newData));
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        // EP: no stored contact shares the queried identity. An empty book is the zero-contact boundary.
        assertFalse(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        // EP: the exact contact is present.
        addressBook.addPerson(ALICE);
        assertTrue(addressBook.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personWithSameIdentityFieldsInAddressBook_returnsTrue() {
        // EP: the identity matches but other details differ; membership uses identity rather than equality.
        addressBook.addPerson(ALICE);
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND)
                .build();
        assertTrue(addressBook.hasPerson(editedAlice));
    }

    @Test
    public void getPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getPersonList().remove(0));
    }

    @Test
    public void isIdenticalTo_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> addressBook.isIdenticalTo(null));
    }

    @Test
    public void isIdenticalTo_sameContactsInSameOrder_returnsTrue() {
        // EP: a copy shares every contact, and the zero-contact boundary is identical to another empty book.
        assertTrue(addressBook.isIdenticalTo(new AddressBook()));
        AddressBook typicalAddressBook = getTypicalAddressBook();
        assertTrue(typicalAddressBook.isIdenticalTo(new AddressBook(typicalAddressBook)));

        // EP: separately built contacts with the same fields and tag spellings.
        addressBook.addPerson(new PersonBuilder(ALICE).withTags("VIP", "friends").build());
        assertTrue(addressBook.isIdenticalTo(new AddressBookStub(
                List.of(new PersonBuilder(ALICE).withTags("friends", "VIP").build()))));
    }

    @Test
    public void isIdenticalTo_tagLetterCaseDiffers_returnsFalse() {
        // EP: tags differ only in letter case, so the books are equal but not identical.
        addressBook.addPerson(new PersonBuilder(ALICE).withTags("VIP", "friends").build());
        AddressBook lowerCaseTags = new AddressBook();
        lowerCaseTags.addPerson(new PersonBuilder(ALICE).withTags("vip", "friends").build());

        assertEquals(addressBook, lowerCaseTags);
        assertFalse(addressBook.isIdenticalTo(lowerCaseTags));
        assertFalse(lowerCaseTags.isIdenticalTo(addressBook));
    }

    @Test
    public void isIdenticalTo_differentContacts_returnsFalse() {
        addressBook.addPerson(ALICE);
        addressBook.addPerson(BENSON);

        // EP: a contact is missing.
        assertFalse(addressBook.isIdenticalTo(new AddressBookStub(List.of(ALICE))));
        // EP: the same contacts in a different order.
        assertFalse(addressBook.isIdenticalTo(new AddressBookStub(List.of(BENSON, ALICE))));
        // EP: a field other than the tags differs, here only the letter case of the name.
        Person lowerCaseAlice = new PersonBuilder(ALICE).withName("alice pauline").build();
        assertFalse(addressBook.isIdenticalTo(new AddressBookStub(List.of(lowerCaseAlice, BENSON))));
    }

    @Test
    public void toStringMethod() {
        String expected = AddressBook.class.getCanonicalName() + "{persons=" + addressBook.getPersonList() + "}";
        assertEquals(expected, addressBook.toString());
    }

    /**
     * A stub ReadOnlyAddressBook whose persons list can violate interface constraints.
     */
    private static class AddressBookStub implements ReadOnlyAddressBook {
        private final ObservableList<Person> persons = FXCollections.observableArrayList();

        AddressBookStub(Collection<Person> persons) {
            this.persons.setAll(persons);
        }

        @Override
        public ObservableList<Person> getPersonList() {
            return persons;
        }
    }

}
