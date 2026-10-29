package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class NameContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        NameContainsKeywordsPredicate firstPredicate = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        NameContainsKeywordsPredicate secondPredicate = new NameContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        NameContainsKeywordsPredicate firstPredicateCopy = new NameContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_nameContainsKeywords_returnsTrue() {
        // EP: one keyword matching a complete name word.
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // EP: multiple keywords that all match name words.
        predicate = new NameContainsKeywordsPredicate(List.of("Alice", "Bob"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // EP: only some keywords match; one match is sufficient because the search uses OR.
        predicate = new NameContainsKeywordsPredicate(List.of("Bob", "Carol"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Carol").build()));

        // EP: matching keywords with different letter case.
        predicate = new NameContainsKeywordsPredicate(List.of("aLIce", "bOB"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // EP: a keyword matching only the start of a name word.
        predicate = new NameContainsKeywordsPredicate(List.of("ta"));
        assertTrue(predicate.test(new PersonBuilder().withName("Alice Tan").build()));
    }

    @Test
    public void test_canonicallyEquivalentKeywords_returnsTrue() {
        Person person = new PersonBuilder().withName("Émile José Núñez").build();

        // EP: complete words and prefixes match in composed, decomposed and mixed-case forms.
        for (String keyword : List.of("Émile", "E\u0301mile", "éM", "e\u0301M",
                "José", "Jose\u0301", "jOsE\u0301", "Nú", "Nu\u0301")) {
            NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of(keyword));
            assertTrue(predicate.test(person), keyword);
        }

        // EP: one canonically equivalent keyword is enough when other keywords do not match.
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of("Nobody", "e\u0301M"));
        assertTrue(predicate.test(person));

        // EP: decomposed Hangul syllables match the same canonical non-Latin name prefix.
        Person hangulPerson = new PersonBuilder().withName("\uac00 Kim").build();
        predicate = new NameContainsKeywordsPredicate(List.of("\u1100\u1161"));
        assertTrue(predicate.test(hangulPerson));
    }

    @Test
    public void test_differentAccentOrMiddleOfWord_returnsFalse() {
        Person person = new PersonBuilder().withName("Émile José Núñez").build();

        // EP: normalization preserves accent distinctions and matches only the start of a word.
        for (String keyword : List.of("Emile", "Jose", "Jòsé", "mile", "ose\u0301")) {
            NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of(keyword));
            assertFalse(predicate.test(person), keyword);
        }
    }

    @Test
    public void test_nameDoesNotContainKeywords_returnsFalse() {
        // EP: no keywords. Boundary value: empty keyword list cannot produce a match.
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // EP: a keyword absent from every name word.
        predicate = new NameContainsKeywordsPredicate(List.of("Carol"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Bob").build()));

        // EP: a substring inside a name word that does not start the word.
        predicate = new NameContainsKeywordsPredicate(List.of("lic"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice Tan").build()));

        // EP: keywords matching other contact fields but no name words.
        predicate = new NameContainsKeywordsPredicate(List.of("12345", "alice@email.com", "Main", "Street"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        NameContainsKeywordsPredicate predicate = new NameContainsKeywordsPredicate(keywords);

        String expected = NameContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
