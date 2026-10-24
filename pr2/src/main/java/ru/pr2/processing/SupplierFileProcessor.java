package ru.pr2.processing;

import ru.pr2.db.SupplierRepository;
import ru.pr2.fs.WorkingDirectories;
import ru.pr2.model.SupplierProduct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public final class SupplierFileProcessor {
    private final WorkingDirectories directories;
    private final ParserRegistry parsers;
    private final ChecksumService checksums;
    private final SupplierRepository repository;

    public SupplierFileProcessor(
            WorkingDirectories directories,
            ParserRegistry parsers,
            ChecksumService checksums,
            SupplierRepository repository) {
        this.directories = directories;
        this.parsers = parsers;
        this.checksums = checksums;
        this.repository = repository;
    }

    public ProcessingResult process(Path source) throws IOException, SQLException {
        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException("Входной файл не существует: " + source);
        }
        if (!directories.isSupported(source)) {
            throw new IllegalArgumentException("Поддерживаются только CSV и JSON: " + source);
        }

        long size = Files.size(source);
        String sha256 = checksums.sha256(source);
        int checksum16 = checksums.checksum16(source);
        List<SupplierProduct> products = parsers.parserFor(source).parse(source);

        repository.saveProcessedFile(
                source.getFileName().toString(), size, sha256, products);
        Path destination = directories.moveToProcessed(source);

        return new ProcessingResult(
                destination.getFileName().toString(),
                size,
                sha256,
                checksum16,
                products.size(),
                destination,
                "processed");
    }
}
