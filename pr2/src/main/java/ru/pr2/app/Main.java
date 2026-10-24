package ru.pr2.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.pr2.db.InMemorySupplierRepository;
import ru.pr2.db.PostgresSupplierRepository;
import ru.pr2.db.SupplierRepository;
import ru.pr2.fs.FileInfo;
import ru.pr2.fs.WorkingDirectories;
import ru.pr2.processing.ChecksumService;
import ru.pr2.processing.CsvSupplierParser;
import ru.pr2.processing.JsonSupplierParser;
import ru.pr2.processing.NioTextReader;
import ru.pr2.processing.ParserRegistry;
import ru.pr2.processing.SupplierFileProcessor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Main {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        boolean noDatabase = arguments.remove("--no-db");
        String command = arguments.isEmpty() ? "demo" : arguments.getFirst();

        Path projectRoot = resolveProjectRoot();
        Path dataRoot = Path.of(System.getProperty(
                "pr2.data.dir", projectRoot.resolve("data").toString()));
        Path samples = Path.of(System.getProperty(
                "pr2.samples.dir", projectRoot.resolve("samples").toString()));
        WorkingDirectories directories = new WorkingDirectories(dataRoot);
        directories.initialize();

        try (SupplierRepository repository = createRepository(noDatabase)) {
            FileWorkflow workflow = createWorkflow(directories, repository);
            switch (command) {
                case "demo" -> {
                    int copied = DemoDataSeeder.seed(samples, directories, false);
                    System.out.println("Подготовлено входных файлов: " + copied);
                    printDirectory(directories);
                    System.out.println("Обработано файлов: " + workflow.processAvailable());
                }
                case "seed" -> {
                    int copied = DemoDataSeeder.seed(samples, directories, false);
                    System.out.println("Скопировано в incoming: " + copied);
                    printDirectory(directories);
                }
                case "process" -> {
                    printDirectory(directories);
                    System.out.println("Обработано файлов: " + workflow.processAvailable());
                }
                case "list" -> printDirectory(directories);
                case "watch" -> {
                    System.out.println("Обработано существующих файлов: " + workflow.processAvailable());
                    long seconds = arguments.size() >= 2 ? Long.parseLong(arguments.get(1)) : 60;
                    new DirectoryWatcher(directories, workflow).watch(Duration.ofSeconds(seconds));
                }
                default -> throw new IllegalArgumentException("Неизвестная команда: " + command);
            }
            System.out.println("Записей товаров в хранилище: " + repository.productCount());
        }
    }

    private static FileWorkflow createWorkflow(
            WorkingDirectories directories, SupplierRepository repository) {
        NioTextReader reader = new NioTextReader();
        ParserRegistry parsers = new ParserRegistry(
                new CsvSupplierParser(reader),
                new JsonSupplierParser(reader, new ObjectMapper()));
        SupplierFileProcessor processor = new SupplierFileProcessor(
                directories, parsers, new ChecksumService(), repository);
        return new FileWorkflow(directories, processor);
    }

    private static SupplierRepository createRepository(boolean noDatabase) throws Exception {
        if (noDatabase) {
            System.out.println("Хранилище: in-memory (--no-db)");
            return new InMemorySupplierRepository();
        }
        String url = System.getProperty("pr2.db.url", "jdbc:postgresql://localhost:5434/suppliers");
        String user = System.getProperty("pr2.db.user", "pr2");
        String password = System.getProperty("pr2.db.password", "pr2");
        System.out.println("Хранилище: PostgreSQL " + url);
        return new PostgresSupplierRepository(url, user, password);
    }

    private static Path resolveProjectRoot() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        if (isProjectRoot(workingDirectory)) {
            return workingDirectory;
        }

        Path nestedProject = workingDirectory.resolve("pr2");
        if (isProjectRoot(nestedProject)) {
            return nestedProject;
        }

        return workingDirectory;
    }

    private static boolean isProjectRoot(Path path) {
        return Files.isRegularFile(path.resolve("pom.xml"))
                && Files.isDirectory(path.resolve("samples"));
    }

    private static void printDirectory(WorkingDirectories directories) throws Exception {
        System.out.println("Каталог incoming: " + directories.incoming());
        List<FileInfo> files = directories.listIncoming();
        if (files.isEmpty()) {
            System.out.println("  (пусто)");
            return;
        }
        for (FileInfo info : files) {
            System.out.printf("  %s | %d bytes | %s%n",
                    info.path().getFileName(), info.size(), DATE_TIME.format(info.lastModified()));
        }
    }
}
