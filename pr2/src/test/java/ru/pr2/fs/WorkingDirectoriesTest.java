package ru.pr2.fs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkingDirectoriesTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void createsAndListsWorkingDirectories() throws Exception {
        WorkingDirectories directories = new WorkingDirectories(temporaryDirectory.resolve("data"));
        directories.initialize();
        Path input = directories.incoming().resolve("supplier.csv");
        Files.writeString(input, "test");

        assertTrue(Files.isDirectory(directories.incoming()));
        assertTrue(Files.isDirectory(directories.processed()));
        assertTrue(Files.isDirectory(directories.failed()));
        assertEquals(1, directories.listIncoming().size());
        assertEquals(4, directories.listIncoming().getFirst().size());
        assertTrue(directories.isSupported(input));
    }
}
