package ru.pr2.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.pr2.db.InMemorySupplierRepository;
import ru.pr2.fs.WorkingDirectories;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupplierFileProcessorTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void processesPersistsAndMovesFile() throws Exception {
        WorkingDirectories directories = new WorkingDirectories(temporaryDirectory.resolve("data"));
        directories.initialize();
        Path input = directories.incoming().resolve("supplier.csv");
        Files.writeString(input, """
                supplierId,supplierName,product,price,quantity
                27,Тестовый поставщик,Товар,125.50,4
                """, StandardCharsets.UTF_8);
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        NioTextReader reader = new NioTextReader();
        SupplierFileProcessor processor = new SupplierFileProcessor(
                directories,
                new ParserRegistry(
                        new CsvSupplierParser(reader),
                        new JsonSupplierParser(reader, new ObjectMapper())),
                new ChecksumService(),
                repository);

        ProcessingResult result = processor.process(input);

        assertEquals("processed", result.status());
        assertEquals(1, result.recordCount());
        assertEquals(64, result.sha256().length());
        assertEquals(1, repository.productCount());
        assertFalse(Files.exists(input));
        assertTrue(Files.exists(directories.processed().resolve("supplier.csv")));
    }
}
