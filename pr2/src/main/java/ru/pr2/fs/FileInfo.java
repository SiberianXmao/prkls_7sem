package ru.pr2.fs;

import java.nio.file.Path;
import java.time.Instant;

public record FileInfo(Path path, long size, Instant lastModified) {
}
