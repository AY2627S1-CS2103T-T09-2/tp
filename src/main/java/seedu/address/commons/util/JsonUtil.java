package seedu.address.commons.util;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;

/**
 * Converts a Java object instance to JSON and vice versa
 */
public class JsonUtil {

    public static final String MESSAGE_TRAILING_CONTENT =
            "Unexpected content after the end of the JSON data at line %d, column %d";

    /**
     * Deepest nesting of objects and arrays accepted when reading. Valid data files are only a few
     * levels deep, while deeply nested junk overflows the stack inside Jackson and crashes the app.
     */
    public static final int MAX_NESTING_DEPTH = 100;

    public static final String MESSAGE_TOO_DEEPLY_NESTED =
            "JSON data is nested more than " + MAX_NESTING_DEPTH + " levels deep at line %d, column %d";

    public static final String MESSAGE_DUPLICATE_KEY = "Duplicate field '%s' at line %d, column %d";

    public static final String MESSAGE_UNPAIRED_SURROGATE =
            "Text contains half of a character (\\u%04X with no matching pair) at line %d, column %d";

    private static final Logger logger = LogsCenter.getLogger(JsonUtil.class);

    private static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            // A repeated key would otherwise silently replace the earlier value, e.g. drop a contact list.
            // requireValidStructure reports repeated keys with their location first; this is a fallback.
            .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);

    static <T> void serializeObjectToJsonFile(Path jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, toJsonString(objectToSerialize));
    }

    static <T> T deserializeObjectFromJsonFile(Path jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        return fromJsonString(FileUtil.readFromFile(jsonFile), classOfObjectToDeserialize);
    }

    /**
     * Returns the JSON object from the given file or {@code Optional.empty()} object if the file is not found.
     * If any values are missing from the file, default values will be used, as long as the file is a valid JSON file.
     *
     * @param filePath cannot be null.
     * @param classOfObjectToDeserialize JSON file has to correspond to the structure in the class given here.
     * @throws DataLoadingException if loading of the JSON file failed.
     */
    public static <T> Optional<T> readJsonFile(
            Path filePath, Class<T> classOfObjectToDeserialize) throws DataLoadingException {
        requireNonNull(filePath);

        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        logger.info("JSON file " + filePath + " found.");

        T jsonFile;

        try {
            jsonFile = deserializeObjectFromJsonFile(filePath, classOfObjectToDeserialize);
        } catch (IOException e) {
            logger.warning("Error reading from jsonFile file " + filePath + ": " + e);
            throw new DataLoadingException(e);
        }

        if (jsonFile == null) {
            throw new DataLoadingException(new IOException("JSON file " + filePath + " contains null."));
        }

        return Optional.of(jsonFile);
    }

    /**
     * Saves the Json object to the specified file.
     * Overwrites existing file if it exists, creates a new file if it doesn't.
     * @param jsonFile cannot be null
     * @param filePath cannot be null
     * @throws IOException if there was an error during writing to the file
     */
    public static <T> void saveJsonFile(T jsonFile, Path filePath) throws IOException {
        requireNonNull(filePath);
        requireNonNull(jsonFile);

        serializeObjectToJsonFile(filePath, jsonFile);
    }


    /**
     * Converts a given string representation of JSON data to an instance of a class.
     * The string must hold exactly one JSON value; only whitespace may follow it.
     * Repeated keys in an object and nesting deeper than {@link #MAX_NESTING_DEPTH} are rejected.
     * @param <T> The generic type to create an instance of
     * @return The instance of T with the specified values in the JSON string
     * @throws IOException if the JSON is invalid, has a repeated key, is nested too deeply,
     *     or there is content after the JSON value
     */
    public static <T> T fromJsonString(String json, Class<T> instanceClass) throws IOException {
        requireValidStructure(json);
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            T instance = objectMapper.readValue(parser, instanceClass);
            requireNoTrailingContent(parser, json);
            return instance;
        }
    }

    /**
     * Throws an {@code IOException} if the first JSON value in {@code json} nests objects and arrays
     * deeper than {@link #MAX_NESTING_DEPTH} or repeats a key within one object. Jackson's object mapping
     * recurses once per level, so this streaming pass, which uses no recursion, has to run first.
     * Repeated keys are checked here too because Jackson 2.7 does not say where the repeated key is,
     * which makes it hard to find in a large data file. Strings and field names holding half of a surrogate
     * pair, which can only come from a Unicode escape in the JSON text, are rejected too: they are not valid text,
     * and saving them would replace them with {@code ?} for good. Only the first value is checked;
     * anything after it is reported by {@link #requireNoTrailingContent(JsonParser, String)} instead.
     */
    private static void requireValidStructure(String json) throws IOException {
        try (JsonParser parser = objectMapper.getFactory().createParser(json)) {
            parser.disable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
            Deque<Set<String>> keysOfOpenObjects = new ArrayDeque<>();
            int depth = 0;
            JsonToken token;
            while ((token = parser.nextToken()) != null) {
                if (token.isStructStart()) {
                    depth++;
                    if (depth > MAX_NESTING_DEPTH) {
                        throw new IOException(formatWithLocation(MESSAGE_TOO_DEEPLY_NESTED, parser));
                    }
                    if (token == JsonToken.START_OBJECT) {
                        keysOfOpenObjects.push(new HashSet<>());
                    }
                } else if (token.isStructEnd()) {
                    depth--;
                    if (token == JsonToken.END_OBJECT) {
                        keysOfOpenObjects.pop();
                    }
                } else if (token == JsonToken.FIELD_NAME) {
                    requireUniqueKey(keysOfOpenObjects, parser);
                }
                if (token == JsonToken.FIELD_NAME || token == JsonToken.VALUE_STRING) {
                    requireNoUnpairedSurrogate(parser);
                }
                if (depth == 0) {
                    return;
                }
            }
        }
    }

    /**
     * Throws an {@code IOException} if the field name at the current token of {@code parser} was
     * already used in the innermost open object, whose keys so far are on top of {@code keysOfOpenObjects}.
     */
    private static void requireUniqueKey(Deque<Set<String>> keysOfOpenObjects, JsonParser parser)
            throws IOException {
        assert !keysOfOpenObjects.isEmpty() : "A field name can only appear inside an object";
        String key = parser.getCurrentName();
        if (!keysOfOpenObjects.peek().add(key)) {
            throw new IOException(formatWithLocation(MESSAGE_DUPLICATE_KEY, parser, key));
        }
    }

    /**
     * Throws an {@code IOException} if the text of the current string or field name token of
     * {@code parser} has a high surrogate that is not followed by a low surrogate, or a low surrogate
     * that does not follow a high surrogate.
     */
    private static void requireNoUnpairedSurrogate(JsonParser parser) throws IOException {
        String text = parser.getText();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isHighSurrogate(c) && i + 1 < text.length()
                    && Character.isLowSurrogate(text.charAt(i + 1))) {
                i++; // A complete pair, such as an escaped emoji.
            } else if (Character.isSurrogate(c)) {
                throw new IOException(formatWithLocation(MESSAGE_UNPAIRED_SURROGATE, parser, (int) c));
            }
        }
    }

    /**
     * Formats {@code message} with {@code arguments} followed by the line and column where the
     * current token of {@code parser} starts.
     */
    private static String formatWithLocation(String message, JsonParser parser, Object... arguments) {
        JsonLocation location = parser.getTokenLocation();
        Object[] allArguments = Arrays.copyOf(arguments, arguments.length + 2);
        allArguments[arguments.length] = location.getLineNr();
        allArguments[arguments.length + 1] = location.getColumnNr();
        return String.format(message, allArguments);
    }

    /**
     * Throws an {@code IOException} if {@code parser}, which just read a JSON value from {@code json},
     * has anything other than whitespace left after that value. Jackson stops after the first value,
     * so without this check a second object or stray text at the end of a data file would be dropped
     * without any error.
     */
    private static void requireNoTrailingContent(JsonParser parser, String json) throws IOException {
        int endOfValue = (int) parser.getCurrentLocation().getCharOffset();
        JsonToken trailingToken;
        try {
            trailingToken = parser.nextToken();
        } catch (JsonProcessingException e) {
            // The leftover text is not even a valid JSON token, e.g. a stray comma or slash.
            throw new IOException(getTrailingContentMessage(json, endOfValue), e);
        }

        if (trailingToken != null) {
            throw new IOException(getTrailingContentMessage(json, endOfValue));
        }
    }

    /**
     * Returns the trailing-content message with the line and column of the first character after
     * {@code endOfValue} that is not JSON whitespace. Jackson's own locations are not reliable here:
     * for a {@code //} comment or a lone {@code /}, its token location is still the end of the JSON
     * value and its error location is past the start of the stray text.
     */
    private static String getTrailingContentMessage(String json, int endOfValue) {
        int line = 1;
        int lineStart = 0;
        int index = 0;
        while (index < json.length() && (index < endOfValue || isJsonWhitespace(json.charAt(index)))) {
            char c = json.charAt(index);
            index++;
            // Like Jackson, count CR LF as one line break and a lone CR as a line break too.
            boolean isCrBeforeLf = c == '\r' && index < json.length() && json.charAt(index) == '\n';
            if (c == '\n' || (c == '\r' && !isCrBeforeLf)) {
                line++;
                lineStart = index;
            }
        }
        return String.format(MESSAGE_TRAILING_CONTENT, line, index - lineStart + 1);
    }

    /**
     * Returns true if {@code c} is one of the four whitespace characters JSON allows between tokens.
     */
    private static boolean isJsonWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
    }

    /**
     * Converts a given instance of a class into its JSON data string representation
     * @param instance The T object to be converted into the JSON string
     * @param <T> The generic type to create an instance of
     * @return JSON data representation of the given class instance, in string
     */
    public static <T> String toJsonString(T instance) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

}
