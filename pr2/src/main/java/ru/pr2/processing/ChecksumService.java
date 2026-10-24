package ru.pr2.processing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ChecksumService {
    private static final int BUFFER_SIZE = 16 * 1024;

    public String sha256(Path path) throws IOException {
        MessageDigest digest = sha256Digest();
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            while (channel.read(buffer) != -1) {
                buffer.flip();
                digest.update(buffer);
                buffer.clear();
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    /** Дополнительная 16-битная сумма из методички, вычисленная бинарными операциями. */
    public int checksum16(Path path) throws IOException {
        int checksum = 0;
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            while (channel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    checksum = (checksum + (buffer.get() & 0xFF)) & 0xFFFF;
                }
                buffer.clear();
            }
        }
        return checksum;
    }

    private MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JVM не поддерживает SHA-256", exception);
        }
    }
}
