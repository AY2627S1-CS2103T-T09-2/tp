package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class CliSyntaxTest {

    @Test
    public void allPrefixes_containsEveryDeclaredPrefix() throws Exception {
        // A prefix left out of ALL_PREFIXES would be saved into fields by add and edit instead of rejected.
        List<Prefix> declaredPrefixes = new ArrayList<>();
        for (Field field : CliSyntax.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == Prefix.class) {
                declaredPrefixes.add((Prefix) field.get(null));
            }
        }

        assertFalse(declaredPrefixes.isEmpty());
        assertEquals(new HashSet<>(declaredPrefixes), new HashSet<>(CliSyntax.ALL_PREFIXES));
        assertEquals(declaredPrefixes.size(), CliSyntax.ALL_PREFIXES.size());
    }
}
