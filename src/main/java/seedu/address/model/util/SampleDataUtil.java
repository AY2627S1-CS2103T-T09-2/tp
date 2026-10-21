package seedu.address.model.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.FollowUp;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Status;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods for populating {@code AddressBook} with sample data.
 */
public class SampleDataUtil {
    public static Person[] getSamplePersons() {
        return new Person[] {
            new Person(new Name("John Tan"), new Phone("91234567"), new Email("johntan@example.com"),
                new Address("21 Clementi Ave 3"), Status.PROSPECT, getTagSet("referral", "hotLead"),
                Optional.of(new FollowUp("01-10-2026"))),
            new Person(new Name("Mei Ling Wong"), new Phone("92345678"), new Email("meiling@example.com"),
                new Address("18 Bishan Street 12"), Status.CLIENT, getTagSet("lifePolicy", "familyPlan"),
                Optional.of(new FollowUp("16-10-2026"))),
            new Person(new Name("Aisha Rahman"), new Phone("93456789"), new Email("aisha@example.com"),
                new Address("7 Tampines Ave 4"), Status.PROSPECT, getTagSet("referral"),
                Optional.of(new FollowUp("05-11-2026"))),
            new Person(new Name("Daniel Lim"), new Phone("94567890"), new Email("daniel@example.com"),
                new Address("42 Ang Mo Kio Ave 10"), Status.CLIENT, getTagSet("lifePolicy"), Optional.empty()),
            new Person(new Name("Priya Nair"), new Phone("95678901"), new Email("priya@example.com"),
                new Address("15 Jurong West Street 65"), Status.PROSPECT, getTagSet("hotLead", "familyPlan"),
                Optional.empty()),
            new Person(new Name("Marcus Lee"), new Phone("96789012"), new Email("marcus@example.com"),
                new Address("33 Bedok North Road"), Status.INACTIVE, getTagSet("referral"), Optional.empty()),
            new Person(new Name("Nur Farah Hassan"), new Phone("97890123"), new Email("farah@example.com"),
                new Address("10 Woodlands Drive 16"), Status.CLIENT, getTagSet("familyPlan"), Optional.empty()),
            new Person(new Name("Wei Ming Chen"), new Phone("98901234"), new Email("weiming@example.com"),
                new Address("28 Bukit Batok Street 23"), Status.INACTIVE, getTagSet("lifePolicy"), Optional.empty())
        };
    }

    public static ReadOnlyAddressBook getSampleAddressBook() {
        AddressBook sampleAb = new AddressBook();
        for (Person samplePerson : getSamplePersons()) {
            sampleAb.addPerson(samplePerson);
        }
        return sampleAb;
    }

    /**
     * Returns a tag set containing the list of strings given.
     */
    public static Set<Tag> getTagSet(String... strings) {
        return Arrays.stream(strings)
                .map(Tag::new)
                .collect(Collectors.toSet());
    }

}
