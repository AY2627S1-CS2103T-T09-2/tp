package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileUtilTest {

    // Emoji (4-byte), CJK (3-byte), Tamil (3-byte), and an e followed by a combining acute accent.
    private static final String NON_ASCII_TEXT = "Blk 1 🏠 东海路 "
            + "சென்னை José";

    @TempDir
    public Path testFolder;

    @Test
    public void readFromFile_validNonAsciiText_decodedExactly() throws Exception {
        // EP: valid UTF-8 with 2, 3 and 4 byte sequences and combining marks.
        Path file = writeBytes(NON_ASCII_TEXT.getBytes(StandardCharsets.UTF_8));
        assertEquals(NON_ASCII_TEXT, FileUtil.readFromFile(file));
    }

    @Test
    public void readAndWrite_validNonAsciiText_bytesUnchanged() throws Exception {
        // EP: reading a valid file and writing it back must not change a single byte.
        byte[] original = ("{\"address\":\"" + NON_ASCII_TEXT + "\"}\n").getBytes(StandardCharsets.UTF_8);
        Path source = writeBytes(original);
        Path copy = testFolder.resolve("copy.json");

        FileUtil.writeToFile(copy, FileUtil.readFromFile(source));

        assertArrayEquals(original, Files.readAllBytes(copy));
    }

    @Test
    public void readFromFile_emptyFile_emptyString() throws Exception {
        // Boundary: no bytes at all is still valid UTF-8.
        assertEquals("", FileUtil.readFromFile(writeBytes(new byte[0])));
    }

    @Test
    public void readFromFile_leadingByteOrderMark_keptAsCharacter() throws Exception {
        // Documents existing behaviour: a UTF-8 byte order mark is valid UTF-8 and is not stripped here.
        Path file = writeBytes(bytes(0xEF, 0xBB, 0xBF, 'a'));
        assertEquals("﻿a", FileUtil.readFromFile(file));
    }

    @Test
    public void readFromFile_invalidByteInText_throwsIoException() throws Exception {
        // EP: a byte that can never appear in UTF-8 (0xFF) in the middle of an address.
        Path file = writeBytes(concat("{\"address\":\"Blk 1", bytes(0xFF), " Road\"}"));
        assertInvalidUtf8(file, 17);
    }

    @Test
    public void readFromFile_truncatedSequenceAtEnd_throwsIoException() throws Exception {
        // Boundary: the file ends partway through a 3-byte character (first 2 bytes of U+4E1C).
        Path file = writeBytes(concat("abc", bytes(0xE4, 0xB8)));
        assertInvalidUtf8(file, 3);
    }

    @Test
    public void readFromFile_overlongEncoding_throwsIoException() throws Exception {
        // EP: '/' encoded in 2 bytes instead of 1, which UTF-8 forbids.
        Path file = writeBytes(concat("a", bytes(0xC0, 0xAF)));
        assertInvalidUtf8(file, 1);
    }

    @Test
    public void readFromFile_encodedSurrogate_throwsIoException() throws Exception {
        // EP: the lone surrogate U+D800 encoded as if it were a character (CESU-8 style).
        Path file = writeBytes(concat("ab", bytes(0xED, 0xA0, 0x80)));
        assertInvalidUtf8(file, 2);
    }

    @Test
    public void readFromFile_unexpectedContinuationByte_throwsIoException() throws Exception {
        // EP: a continuation byte with no lead byte, e.g. a Latin-1 file saved over a UTF-8 one.
        Path file = writeBytes(bytes(0x80));
        assertInvalidUtf8(file, 0);
    }

    private void assertInvalidUtf8(Path file, int expectedOffset) {
        String expectedMessage = String.format(FileUtil.MESSAGE_INVALID_UTF8, file, expectedOffset);
        assertThrows(IOException.class, expectedMessage, () -> FileUtil.readFromFile(file));
    }

    private Path writeBytes(byte[] content) throws IOException {
        Path file = testFolder.resolve("data.json");
        Files.write(file, content);
        assertTrue(Files.exists(file));
        return file;
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    private static byte[] concat(Object... parts) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (Object part : parts) {
            byte[] partBytes = part instanceof String text ? text.getBytes(StandardCharsets.UTF_8) : (byte[]) part;
            out.writeBytes(partBytes);
        }
        return out.toByteArray();
    }
}
