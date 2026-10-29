package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.commons.util.JsonUtil.MAX_NESTING_DEPTH;
import static seedu.address.commons.util.JsonUtil.MESSAGE_TOO_DEEPLY_NESTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;

/**
 * Tests that {@link JsonUtil} rejects repeated keys and deeply nested JSON when reading.
 */
public class JsonUtilStrictReadingTest {

    private static final String DUPLICATE_FIELD_MESSAGE = "Duplicate field '%s'";

    @TempDir
    public Path testFolder;

    @Test
    public void fromJsonString_repeatedKey_throwsIoException() {
        // EP: the same key twice in one object, at the top level and inside nested objects.
        assertDuplicateRejected("{\"name\":\"first\",\"name\":\"second\"}", "name");
        assertDuplicateRejected("{\"name\":\"first\",\"listOfLocalDateTimes\":[],\"name\":null}", "name");
        assertDuplicateRejected("{\"mapOfIntegerToString\":{\"1\":\"One\",\"1\":\"Uno\"}}", "1");
        assertDuplicateRejected("{\"name\":\"a\",\"unknown\":{\"x\":1,\"x\":2}}", "x");
        assertDuplicateRejected("[{\"a\":1},{\"b\":{\"c\":[{\"d\":1,\"d\":1}]}}]", "d");
    }

    @Test
    public void fromJsonString_repeatedKey_messageGivesLocation() {
        // The location is where the repeated key starts, so it can be found in a large data file.
        IOException e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(
                "{\n  \"name\" : \"first\",\n  \"mapOfIntegerToString\" : { },\n  \"name\" : \"second\"\n}",
                SerializableTestClass.class));
        assertEquals(String.format(JsonUtil.MESSAGE_DUPLICATE_KEY, "name", 4, 3), e.getMessage());

        e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(
                "[{\"a\":1},\n{\"b\":{\"c\":2,\n    \"c\":3}}]", Object.class));
        assertEquals(String.format(JsonUtil.MESSAGE_DUPLICATE_KEY, "c", 3, 5), e.getMessage());

        // A repeated key that comes before too deep nesting is reported first, and the other way round.
        e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(
                "{\"a\":1,\"a\":" + nestedArrays(MAX_NESTING_DEPTH) + "}", Object.class));
        assertEquals(String.format(JsonUtil.MESSAGE_DUPLICATE_KEY, "a", 1, 8), e.getMessage());
        e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(
                "{\"a\":" + nestedArrays(MAX_NESTING_DEPTH) + ",\"a\":1}", Object.class));
        assertEquals(String.format(MESSAGE_TOO_DEEPLY_NESTED, 1, MAX_NESTING_DEPTH + 5), e.getMessage());
    }

    @Test
    public void fromJsonString_sameKeyInDifferentObjects_success() throws IOException {
        // EP: a key may repeat across sibling or nested objects; only repeats within one object are errors.
        assertEquals(List.of(Map.of("a", 1), Map.of("a", 2)),
                JsonUtil.fromJsonString("[{\"a\":1},{\"a\":2}]", Object.class));
        assertEquals(Map.of("a", Map.of("a", 1)), JsonUtil.fromJsonString("{\"a\":{\"a\":1}}", Object.class));
        assertEquals("first", JsonUtil.fromJsonString(
                "{\"name\":\"first\",\"mapOfIntegerToString\":{\"1\":\"name\"}}", SerializableTestClass.class)
                .getName());
    }

    @Test
    public void readJsonFile_repeatedKey_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("data.json");
        Files.writeString(filePath, "{\"name\":\"first\",\"name\":\"second\"}");

        DataLoadingException e = assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(filePath, SerializableTestClass.class));
        assertTrue(e.getCause().getMessage().startsWith(String.format(DUPLICATE_FIELD_MESSAGE, "name")),
                e.getCause().getMessage());
    }

    @Test
    public void fromJsonString_nestingAtLimit_success() {
        // Boundary: exactly MAX_NESTING_DEPTH levels is still accepted.
        assertDoesNotThrow(() -> JsonUtil.fromJsonString(nestedArrays(MAX_NESTING_DEPTH), Object.class));
        assertDoesNotThrow(() -> JsonUtil.fromJsonString(nestedObjects(MAX_NESTING_DEPTH), Object.class));
        assertDoesNotThrow(() -> JsonUtil.fromJsonString(
                "{\"name\":\"a\",\"junk\":" + nestedArrays(MAX_NESTING_DEPTH - 1) + "}",
                SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_nestingAboveLimit_throwsIoException() {
        // Boundary: one level past the limit, then depths that used to overflow the stack.
        for (int depth : new int[] {MAX_NESTING_DEPTH + 1, 1_000, 10_000, 100_000}) {
            assertTooDeep(nestedArrays(depth), 1, MAX_NESTING_DEPTH + 1);
            assertTooDeep(nestedObjects(depth), 1, 5 * MAX_NESTING_DEPTH + 1);
            assertTooDeep("[" + nestedArrays(depth - 1) + "]", 1, MAX_NESTING_DEPTH + 1);
            assertTooDeep("{\"junk\":" + nestedArrays(depth - 1) + ",\"name\":\"a\"}", 1, MAX_NESTING_DEPTH + 8);
            assertTooDeep("\n  " + "[".repeat(depth), 2, MAX_NESTING_DEPTH + 3);
        }
    }

    @Test
    public void readJsonFile_nestingAboveLimit_throwsDataLoadingException() throws IOException {
        Path filePath = testFolder.resolve("data.json");
        Files.writeString(filePath, "{\"name\":\"a\",\"junk\":" + nestedObjects(50_000) + "}");

        DataLoadingException e = assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(filePath, SerializableTestClass.class));
        assertInstanceOf(IOException.class, e.getCause());
        assertTrue(e.getCause().getMessage().startsWith("JSON data is nested more than"),
                e.getCause().getMessage());
    }

    @Test
    public void fromJsonString_deepContentAfterValue_reportsTrailingContent() {
        // Only the first value is checked for depth; anything after it is reported as extra content.
        IOException e = assertThrows(IOException.class, () ->
                JsonUtil.fromJsonString("{} " + nestedArrays(50_000), SerializableTestClass.class));
        assertEquals(String.format(JsonUtil.MESSAGE_TRAILING_CONTENT, 1, 4), e.getMessage());
    }

    private static void assertDuplicateRejected(String json, String key) {
        Class<?> target = json.startsWith("[") ? Object.class : SerializableTestClass.class;
        IOException e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, target));
        assertTrue(e.getMessage().startsWith(String.format(DUPLICATE_FIELD_MESSAGE, key)), e.getMessage());
    }

    private static void assertTooDeep(String json, int line, int column) {
        for (Class<?> target : List.of(Object.class, SerializableTestClass.class)) {
            IOException e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, target));
            assertEquals(String.format(MESSAGE_TOO_DEEPLY_NESTED, line, column), e.getMessage());
        }
    }

    /** Returns {@code depth} nested empty arrays, e.g. {@code [[]]} for a depth of 2. */
    private static String nestedArrays(int depth) {
        return "[".repeat(depth) + "]".repeat(depth);
    }

    /** Returns {@code depth} nested objects, e.g. <code>{"a":{}}</code> for a depth of 2. */
    private static String nestedObjects(int depth) {
        return "{\"a\":".repeat(depth - 1) + "{}" + "}".repeat(depth - 1);
    }
}
