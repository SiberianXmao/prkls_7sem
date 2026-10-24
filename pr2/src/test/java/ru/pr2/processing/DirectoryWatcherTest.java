package ru.pr2.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.pr2.app.DirectoryWatcher;
import ru.pr2.app.FileWorkflow;
import ru.pr2.db.InMemorySupplierRepository;
import ru.pr2.fs.WorkingDirectories;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryWatcherTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void detectsAndProcessesNewFile() throws Exception {
        WorkingDirectories directories = new WorkingDirectories(temporaryDirectory.resolve("data"));
        directories.initialize();
        InMemorySupplierRepository repository = new InMemorySupplierRepository();
        NioTextReader reader = new NioTextReader();
        SupplierFileProcessor processor = new SupplierFileProcessor(
                directories,
                new ParserRegistry(
                        new CsvSupplierParser(reader),
                        new JsonSupplierParser(reader, new ObjectMapper())),
                new ChecksumService(),
                repository);
        DirectoryWatcher watcher = new DirectoryWatcher(
                directories, new FileWorkflow(directories, processor));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<?> watching = executor.submit(() -> {
                watcher.watch(Duration.ofSeconds(2));
                return null;
            });
            Thread.sleep(250);
            Files.writeString(directories.incoming().resolve("watched.csv"), """
                    supplierId,supplierName,product,price,quantity
                    99,Watcher,Автоматически обработанный товар,10.00,2
                    """, StandardCharsets.UTF_8);

            watching.get(3, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        assertTrue(Files.exists(directories.processed().resolve("watched.csv")));
        assertEquals(1, repository.productCount());
    }
}
