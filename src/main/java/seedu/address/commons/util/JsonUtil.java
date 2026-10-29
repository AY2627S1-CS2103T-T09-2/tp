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
            requireNoTrailingContent(parser);
            return instance;
        }
    }

    /**
     * Throws an {@code IOException} if the first JSON value in {@code json} nests objects and arrays
     * deeper than {@link #MAX_NESTING_DEPTH} or repeats a key within one object. Jackson's object mapping
     * recurses once per level, so this streaming pass, which uses no recursion, has to run first.
     * Repeated keys are checked here too because Jackson 2.7 does not say where the repeated key is,
     * which makes it hard to find in a large data file. Only the first value is checked; anything after
     * it is reported by {@link #requireNoTrailingContent(JsonParser)} instead.
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
                } else if (token == JsonToken.FIELD_NAME && !keysOfOpenObjects.peek().add(parser.getCurrentName())) {
                    throw new IOException(formatWithLocation(MESSAGE_DUPLICATE_KEY, parser,
                            parser.getCurrentName()));
                }
                if (depth == 0) {
                    return;
                }
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
     * Throws an {@code IOException} if {@code parser} has anything other than whitespace left after
     * the JSON value it just read. Jackson stops after the first value, so without this check a
     * second object or stray text at the end of a data file would be dropped without any error.
     */
    private static void requireNoTrailingContent(JsonParser parser) throws IOException {
        JsonToken trailingToken;
        try {
            trailingToken = parser.nextToken();
        } catch (JsonProcessingException e) {
            // The leftover text is not even a valid JSON token, e.g. a stray comma or brace.
            // The token location is usually closer to the stray text than the exception's location,
            // though for some characters it can still point at the end of the JSON value.
            throw new IOException(getTrailingContentMessage(parser), e);
        }

        if (trailingToken != null) {
            throw new IOException(getTrailingContentMessage(parser));
        }
    }

    private static String getTrailingContentMessage(JsonParser parser) {
        JsonLocation location = parser.getTokenLocation();
        return String.format(MESSAGE_TRAILING_CONTENT, location.getLineNr(), location.getColumnNr());
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
