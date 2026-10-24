package ru.pr2.processing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChecksumServiceTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void calculatesSha256And16BitChecksum() throws Exception {
        Path file = temporaryDirectory.resolve("abc.txt");
        Files.writeString(file, "abc");
        ChecksumService service = new ChecksumService();

        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                service.sha256(file));
        assertEquals(('a' + 'b' + 'c') & 0xFFFF, service.checksum16(file));
    }
}
