package ru.pr2.app;

import ru.pr2.fs.WorkingDirectories;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class DirectoryWatcher {
    private final WorkingDirectories directories;
    private final FileWorkflow workflow;

    public DirectoryWatcher(WorkingDirectories directories, FileWorkflow workflow) {
        this.directories = directories;
        this.workflow = workflow;
    }

    public void watch(Duration duration) throws IOException, InterruptedException {
        if (duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Длительность наблюдения должна быть положительной");
        }

        long deadline = System.nanoTime() + duration.toNanos();
        try (WatchService watchService = directories.incoming().getFileSystem().newWatchService()) {
            directories.incoming().register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            System.out.printf("WatchService наблюдает за %s в течение %d секунд.%n",
                    directories.incoming(), duration.toSeconds());

            while (System.nanoTime() < deadline) {
                long remaining = deadline - System.nanoTime();
                WatchKey key = watchService.poll(remaining, TimeUnit.NANOSECONDS);
                if (key == null) {
                    break;
                }
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    Path relative = (Path) event.context();
                    Path created = directories.incoming().resolve(relative);
                    System.out.println("Обнаружен новый файл: " + relative);
                    if (directories.isSupported(created) && awaitStableFile(created)) {
                        workflow.processSafely(created);
                    }
                }
                if (!key.reset()) {
                    break;
                }
            }
        }
        System.out.println("Наблюдение завершено.");
    }

    private boolean awaitStableFile(Path path) throws InterruptedException {
        long previousSize = -1;
        int stableChecks = 0;
        for (int attempt = 0; attempt < 30; attempt++) {
            try {
                if (Files.isRegularFile(path)) {
                    long currentSize = Files.size(path);
                    stableChecks = currentSize == previousSize ? stableChecks + 1 : 0;
                    previousSize = currentSize;
                    if (stableChecks >= 2) {
                        return true;
                    }
                }
            } catch (IOException ignored) {
                // Файл может быть временно занят копирующим процессом; повторяем проверку.
            }
            Thread.sleep(100);
        }
        return false;
    }
}
