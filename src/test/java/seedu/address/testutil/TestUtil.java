package seedu.address.testutil;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import seedu.address.commons.core.index.Index;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * A utility class for test cases.
 */
public class TestUtil {

    /**
     * Folder used for temp files created during testing. Ignored by Git.
     */
    private static final Path SANDBOX_FOLDER = Paths.get("src", "test", "data", "sandbox");

    /**
     * Appends {@code fileName} to the sandbox folder path and returns the resulting path.
     * Creates the sandbox folder if it doesn't exist.
     */
    public static Path getFilePathInSandboxFolder(String fileName) {
        try {
            Files.createDirectories(SANDBOX_FOLDER);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return SANDBOX_FOLDER.resolve(fileName);
    }

    /**
     * Returns the middle index of the person in the {@code model}'s person list.
     */
    public static Index getMidIndex(Model model) {
        return Index.fromOneBased(model.getFilteredPersonList().size() / 2);
    }

    /**
     * Returns the last index of the person in the {@code model}'s person list.
     */
    public static Index getLastIndex(Model model) {
        return Index.fromOneBased(model.getFilteredPersonList().size());
    }

    /**
     * Returns the person in the {@code model}'s person list at {@code index}.
     */
    public static Person getPerson(Model model, Index index) {
        return model.getFilteredPersonList().get(index.getZeroBased());
    }

    /**
     * Returns the bytes whose unsigned values are {@code values}, such as {@code bytes(0xEF, 0xBB, 0xBF)}.
     */
    public static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    /**
     * Returns {@code parts} joined in order. Each part is either a {@code byte[]}, used as is, or a
     * {@code String}, encoded as UTF-8.
     */
    public static byte[] concat(Object... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Object part : parts) {
            byte[] partBytes = part instanceof String text ? text.getBytes(StandardCharsets.UTF_8) : (byte[]) part;
            out.writeBytes(partBytes);
        }
        return out.toByteArray();
    }

    /**
     * Returns a copy of {@code original} with {@code inserted} placed right after the first {@code marker}.
     */
    public static byte[] insertAfter(byte[] original, String marker, byte[] inserted) {
        // ISO-8859-1 maps each byte to one char, so the index found is also the byte offset.
        int markerIndex = new String(original, StandardCharsets.ISO_8859_1).indexOf(marker);
        assertTrue(markerIndex >= 0, "Marker not found: " + marker);
        int splitAt = markerIndex + marker.length();
        byte[] result = new byte[original.length + inserted.length];
        System.arraycopy(original, 0, result, 0, splitAt);
        System.arraycopy(inserted, 0, result, splitAt, inserted.length);
        System.arraycopy(original, splitAt, result, splitAt + inserted.length, original.length - splitAt);
        return result;
    }
}
