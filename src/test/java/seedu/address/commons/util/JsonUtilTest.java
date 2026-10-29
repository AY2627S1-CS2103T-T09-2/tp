package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");
    private static final String VALID_JSON = SerializableTestClass.JSON_STRING_REPRESENTATION;

    @TempDir
    public Path testFolder;

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertTestValues(serializableTestClass);
    }

    @Test
    public void readJsonFile_missingFile_emptyResult() throws DataLoadingException {
        assertFalse(JsonUtil.readJsonFile(testFolder.resolve("missing.json"), SerializableTestClass.class)
                .isPresent());
    }

    @Test
    public void readJsonFile_validFile_readsValues() throws Exception {
        assertTestValues(readJson(VALID_JSON));
    }

    @Test
    public void readJsonFile_trailingWhitespace_readsValues() throws Exception {
        // EP: only whitespace after the value, e.g. the newline most editors add at the end of a file.
        assertTestValues(readJson(VALID_JSON + "\n"));
        assertTestValues(readJson(VALID_JSON + "\r\n"));
        assertTestValues(readJson(VALID_JSON + " \t\n\n  \r\n"));
    }

    @Test
    public void readJsonFile_leadingWhitespace_readsValues() throws Exception {
        assertTestValues(readJson("\n  \t" + VALID_JSON));
    }

    @Test
    public void readJsonFile_writeThenRead_sameValues() throws Exception {
        SerializableTestClass original = new SerializableTestClass();
        original.setTestValues();
        Path filePath = testFolder.resolve("data.json");

        JsonUtil.saveJsonFile(original, filePath);

        assertTestValues(JsonUtil.readJsonFile(filePath, SerializableTestClass.class).get());
    }

    @Test
    public void readJsonFile_secondObjectAfterValue_throwsDataLoadingException() {
        // EP: a second valid JSON value after the first one, e.g. two data files pasted together.
        assertTrailingContentRejected(VALID_JSON + VALID_JSON);
        assertTrailingContentRejected(VALID_JSON + "\n" + VALID_JSON);
        assertTrailingContentRejected(VALID_JSON + " {}");
        assertTrailingContentRejected(VALID_JSON + " []");
    }

    @Test
    public void readJsonFile_commaAndObjectAfterValue_throwsDataLoadingException() {
        // EP: a contact pasted after the closing brace instead of inside the list.
        assertTrailingContentRejected(VALID_JSON + ",\n" + VALID_JSON);
        assertTrailingContentRejected(VALID_JSON + ",");
    }

    @Test
    public void readJsonFile_extraClosingBracket_throwsDataLoadingException() {
        assertTrailingContentRejected(VALID_JSON + "\n}");
        assertTrailingContentRejected(VALID_JSON + "]");
    }

    @Test
    public void readJsonFile_textAfterValue_throwsDataLoadingException() {
        // EP: stray text that is not JSON at all.
        assertTrailingContentRejected(VALID_JSON + " this is junk");
        assertTrailingContentRejected(VALID_JSON + "x");
        assertTrailingContentRejected(VALID_JSON + "// comment");
        assertTrailingContentRejected(VALID_JSON + "\uFEFF");
    }

    @Test
    public void readJsonFile_otherSpaceCharactersAfterValue_throwsDataLoadingException() {
        // EP: only spaces, tabs and line breaks may follow the value, as the user guide says.
        assertTrailingContentRejected(VALID_JSON + "\f");
        assertTrailingContentRejected(VALID_JSON + "\u000B");
        assertTrailingContentRejected(VALID_JSON + "\u00A0");
        assertTrailingContentRejected(VALID_JSON + "\u3000");
    }

    @Test
    public void readJsonFile_scalarAfterValue_throwsDataLoadingException() {
        // EP: valid JSON literals after the value are still extra content.
        assertTrailingContentRejected(VALID_JSON + " 5");
        assertTrailingContentRejected(VALID_JSON + " \"text\"");
        assertTrailingContentRejected(VALID_JSON + " null");
        assertTrailingContentRejected(VALID_JSON + " true");
    }

    @Test
    public void readJsonFile_emptyFile_throwsDataLoadingException() {
        // EP: no JSON value at all.
        assertThrows(DataLoadingException.class, () -> readJson(""));
        assertThrows(DataLoadingException.class, () -> readJson(" \n\t\r\n"));
    }

    @Test
    public void readJsonFile_jsonNull_throwsDataLoadingException() {
        // EP: a JSON null is rejected, with or without trailing whitespace or content.
        assertThrows(DataLoadingException.class, () -> readJson("null"));
        assertThrows(DataLoadingException.class, () -> readJson("null\n"));
        assertThrows(DataLoadingException.class, () -> readJson("null junk"));
    }

    @Test
    public void readJsonFile_truncatedValue_throwsDataLoadingException() {
        // EP: the value itself is incomplete, which was already rejected before this check.
        assertThrows(DataLoadingException.class, () ->
                readJson(VALID_JSON.substring(0, VALID_JSON.length() - 1)));
    }

    @Test
    public void readJsonFile_invalidUtf8InValue_throwsDataLoadingException() throws Exception {
        // EP: well-formed JSON whose text has a byte that is not valid UTF-8, e.g. 0xFF in a value.
        Path filePath = testFolder.resolve("data.json");
        byte[] prefix = "{\"name\":\"Blk 1".getBytes(StandardCharsets.UTF_8);
        byte[] suffix = " Road\"}".getBytes(StandardCharsets.UTF_8);
        byte[] content = new byte[prefix.length + 1 + suffix.length];
        System.arraycopy(prefix, 0, content, 0, prefix.length);
        content[prefix.length] = (byte) 0xFF;
        System.arraycopy(suffix, 0, content, prefix.length + 1, suffix.length);
        Files.write(filePath, content);

        DataLoadingException e = assertThrows(DataLoadingException.class, () ->
                JsonUtil.readJsonFile(filePath, SerializableTestClass.class));
        assertEquals(String.format(FileUtil.MESSAGE_INVALID_UTF8, filePath, prefix.length),
                e.getCause().getMessage());
    }

    @Test
    public void fromJsonString_trailingContent_messageGivesLocation() {
        // A second value: the location is where that value starts.
        IOException e = assertThrows(IOException.class, () ->
                JsonUtil.fromJsonString("{}\n  {}", SerializableTestClass.class));
        assertEquals(String.format(JsonUtil.MESSAGE_TRAILING_CONTENT, 2, 3), e.getMessage());

        // Text that is not a JSON token: the cause keeps Jackson's own error.
        e = assertThrows(IOException.class, () ->
                JsonUtil.fromJsonString("{} junk", SerializableTestClass.class));
        assertEquals(String.format(JsonUtil.MESSAGE_TRAILING_CONTENT, 1, 4), e.getMessage());
        assertInstanceOf(IOException.class, e.getCause());

        // A stray comma or closing bracket straight after the value.
        e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString("{},{}", SerializableTestClass.class));
        assertEquals(String.format(JsonUtil.MESSAGE_TRAILING_CONTENT, 1, 3), e.getMessage());
        e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString("{}\n}", SerializableTestClass.class));
        assertEquals(String.format(JsonUtil.MESSAGE_TRAILING_CONTENT, 2, 1), e.getMessage());
    }

    @Test
    public void fromJsonString_slashAfterValue_messageGivesLocationOfSlash() {
        // EP: a comment or a lone slash. Jackson reports these at the closing brace, not at the slash.
        assertTrailingContentLocation("{\"persons\":[]}\n\n// note", 3, 1);
        assertTrailingContentLocation("{\"persons\":[]}\n\n/", 3, 1);
        assertTrailingContentLocation("{\"persons\":[]} /* note */", 1, 16);
        assertTrailingContentLocation("{}\r\n\t//", 2, 2);
        assertTrailingContentLocation("{}\r\r/", 3, 1);
    }

    @Test
    public void fromJsonString_singleValue_success() throws IOException {
        assertTestValues(JsonUtil.fromJsonString(VALID_JSON, SerializableTestClass.class));
        assertEquals(Integer.valueOf(5), JsonUtil.fromJsonString(" 5 \n", Integer.class));
    }

    private SerializableTestClass readJson(String content) throws IOException, DataLoadingException {
        Path filePath = testFolder.resolve("data.json");
        Files.writeString(filePath, content);
        return JsonUtil.readJsonFile(filePath, SerializableTestClass.class).get();
    }

    private static void assertTrailingContentLocation(String json, int line, int column) {
        IOException e = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, Object.class));
        assertEquals(String.format(JsonUtil.MESSAGE_TRAILING_CONTENT, line, column), e.getMessage());
    }

    private void assertTrailingContentRejected(String content) {
        DataLoadingException e = assertThrows(DataLoadingException.class, () -> readJson(content));
        assertTrue(e.getCause().getMessage().startsWith("Unexpected content after the end of the JSON data"),
                e.getCause().getMessage());
    }

    private static void assertTestValues(SerializableTestClass serializableTestClass) {
        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }
}
