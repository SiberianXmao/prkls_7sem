package ru.pr2.processing;

import java.nio.file.Path;

public record ProcessingResult(
        String fileName,
        long size,
        String sha256,
        int checksum16,
        int recordCount,
        Path processedPath,
        String status) {
}
