package ru.pr2.fs;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class WorkingDirectories {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("csv", "json");

    private final Path root;
    private final Path incoming;
    private final Path processed;
    private final Path failed;

    public WorkingDirectories(Path root) {
        this.root = root.toAbsolutePath().normalize();
        this.incoming = this.root.resolve("incoming");
        this.processed = this.root.resolve("processed");
        this.failed = this.root.resolve("failed");
    }

    public void initialize() throws IOException {
        Files.createDirectories(incoming);
        Files.createDirectories(processed);
        Files.createDirectories(failed);
    }

    public List<FileInfo> listIncoming() throws IOException {
        try (var paths = Files.list(incoming)) {
            return paths.filter(Files::isRegularFile)
                    .map(this::readInfo)
                    .sorted(Comparator.comparing(info -> info.path().getFileName().toString()))
                    .toList();
        }
    }

    public List<Path> supportedIncomingFiles() throws IOException {
        return listIncoming().stream()
                .map(FileInfo::path)
                .filter(this::isSupported)
                .toList();
    }

    public boolean isSupported(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot >= 0
                && SUPPORTED_EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
    }

    public Path moveToProcessed(Path source) throws IOException {
        return move(source, processed.resolve(source.getFileName()));
    }

    public Path moveToFailed(Path source) throws IOException {
        return move(source, failed.resolve(source.getFileName()));
    }

    private Path move(Path source, Path target) throws IOException {
        try {
            return Files.move(source, target,
                    StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            return Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private FileInfo readInfo(Path path) {
        try {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
            return new FileInfo(path, attributes.size(), attributes.lastModifiedTime().toInstant());
        } catch (IOException exception) {
            throw new FileInfoReadException(path, exception);
        }
    }

    public Path root() {
        return root;
    }

    public Path incoming() {
        return incoming;
    }

    public Path processed() {
        return processed;
    }

    public Path failed() {
        return failed;
    }

    private static final class FileInfoReadException extends RuntimeException {
        private FileInfoReadException(Path path, IOException cause) {
            super("Не удалось прочитать атрибуты " + path, cause);
        }
    }
}
