package ru.pr2.processing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Читает UTF-8 через FileChannel и прямой ByteBuffer. */
public final class NioTextReader {
    private static final int BUFFER_SIZE = 8 * 1024;

    public String read(Path path) throws IOException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        ByteBuffer bytes = ByteBuffer.allocateDirect(BUFFER_SIZE);
        CharBuffer characters = CharBuffer.allocate(BUFFER_SIZE);
        StringBuilder result = new StringBuilder();

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            while (channel.read(bytes) != -1) {
                bytes.flip();
                decode(decoder, bytes, characters, result, false);
                bytes.compact();
            }
            bytes.flip();
            decode(decoder, bytes, characters, result, true);
            flush(decoder, characters, result);
        }
        return result.toString();
    }

    private void decode(
            CharsetDecoder decoder,
            ByteBuffer bytes,
            CharBuffer characters,
            StringBuilder target,
            boolean endOfInput) throws IOException {
        while (true) {
            CoderResult status = decoder.decode(bytes, characters, endOfInput);
            append(characters, target);
            if (status.isUnderflow()) {
                return;
            }
            if (status.isError()) {
                status.throwException();
            }
        }
    }

    private void flush(CharsetDecoder decoder, CharBuffer characters, StringBuilder target)
            throws IOException {
        while (true) {
            CoderResult status = decoder.flush(characters);
            append(characters, target);
            if (status.isUnderflow()) {
                return;
            }
            if (status.isError()) {
                status.throwException();
            }
        }
    }

    private void append(CharBuffer characters, StringBuilder target) {
        characters.flip();
        target.append(characters);
        characters.clear();
    }
}
