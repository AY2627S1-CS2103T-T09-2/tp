package seedu.address.commons.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes and reads files
 */
public class FileUtil {

    public static final String MESSAGE_INVALID_UTF8 =
            "File %s is not valid UTF-8 text: invalid byte sequence at byte %d";

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Creates a file if it does not exist along with its missing parent directories.
     * @throws IOException if the file or directory cannot be created.
     */
    public static void createIfMissing(Path file) throws IOException {
        if (Files.exists(file)) {
            return;
        }

        createParentDirsOfFile(file);

        Files.createFile(file);
    }

    /**
     * Creates parent directories of file if it has a parent directory
     */
    private static void createParentDirsOfFile(Path file) throws IOException {
        Path parentDir = file.getParent();

        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
    }

    /**
     * Returns the contents of {@code file} decoded as UTF-8. Assumes file exists.
     * A leading byte order mark is kept as the character U+FEFF.
     *
     * @throws IOException if the file cannot be read or is not valid UTF-8. Invalid bytes are
     *     reported instead of being replaced with U+FFFD, which would otherwise be saved over the
     *     original text the next time the file is written.
     */
    public static String readFromFile(Path file) throws IOException {
        ByteBuffer bytes = ByteBuffer.wrap(Files.readAllBytes(file));
        CharsetDecoder decoder = CHARSET.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(bytes).toString();
        } catch (CharacterCodingException e) {
            // The decoder stops with the buffer positioned at the start of the invalid sequence.
            throw new IOException(String.format(MESSAGE_INVALID_UTF8, file, bytes.position()), e);
        }
    }

    /**
     * Writes given string to a file.
     * Will create the file if it does not exist yet.
     */
    public static void writeToFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(CHARSET));
    }

}
