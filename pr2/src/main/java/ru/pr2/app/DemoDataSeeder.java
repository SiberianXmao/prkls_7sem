package ru.pr2.app;

import ru.pr2.fs.WorkingDirectories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

final class DemoDataSeeder {
    private DemoDataSeeder() {
    }

    static int seed(Path samples, WorkingDirectories directories, boolean includeWatchSample)
            throws IOException {
        if (!Files.isDirectory(samples)) {
            throw new IOException("Каталог примеров не найден: " + samples);
        }
        int copied = 0;
        try (var files = Files.list(samples)) {
            for (Path source : files.filter(Files::isRegularFile).toList()) {
                boolean watchSample = source.getFileName().toString().contains("watch");
                if (directories.isSupported(source) && (includeWatchSample || !watchSample)) {
                    Files.copy(source, directories.incoming().resolve(source.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                    copied++;
                }
            }
        }
        return copied;
    }
}
