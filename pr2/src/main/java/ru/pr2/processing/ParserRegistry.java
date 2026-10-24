package ru.pr2.processing;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

public final class ParserRegistry {
    private final Map<String, SupplierFileParser> parsers;

    public ParserRegistry(SupplierFileParser csvParser, SupplierFileParser jsonParser) {
        this.parsers = Map.of("csv", csvParser, "json", jsonParser);
    }

    public SupplierFileParser parserFor(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
        SupplierFileParser parser = parsers.get(extension);
        if (parser == null) {
            throw new IllegalArgumentException("Неподдерживаемый формат файла: " + name);
        }
        return parser;
    }
}
