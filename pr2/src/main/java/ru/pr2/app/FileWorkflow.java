package ru.pr2.app;

import ru.pr2.fs.WorkingDirectories;
import ru.pr2.processing.ProcessingResult;
import ru.pr2.processing.SupplierFileProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileWorkflow {
    private final WorkingDirectories directories;
    private final SupplierFileProcessor processor;

    public FileWorkflow(WorkingDirectories directories, SupplierFileProcessor processor) {
        this.directories = directories;
        this.processor = processor;
    }

    public int processAvailable() throws IOException {
        int processed = 0;
        for (Path path : directories.supportedIncomingFiles()) {
            if (processSafely(path)) {
                processed++;
            }
        }
        return processed;
    }

    public boolean processSafely(Path path) {
        try {
            ProcessingResult result = processor.process(path);
            printResult(result);
            return true;
        } catch (Exception exception) {
            System.err.printf("Ошибка обработки %s: %s%n", path.getFileName(), exception.getMessage());
            moveToFailedIfPossible(path);
            return false;
        }
    }

    private void moveToFailedIfPossible(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Path failed = directories.moveToFailed(path);
            System.err.println("Файл перемещён в failed: " + failed);
        } catch (IOException moveError) {
            System.err.println("Не удалось переместить файл в failed: " + moveError.getMessage());
        }
    }

    private void printResult(ProcessingResult result) {
        System.out.println("Файл: " + result.fileName());
        System.out.println("Size: " + result.size() + " bytes");
        System.out.println("SHA-256: " + result.sha256());
        System.out.printf("Checksum-16: %04X%n", result.checksum16());
        System.out.println("Records: " + result.recordCount());
        System.out.println("Status: " + result.status());
        System.out.println("Moved to: " + result.processedPath());
        System.out.println();
    }
}
