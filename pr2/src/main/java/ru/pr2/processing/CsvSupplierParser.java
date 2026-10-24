package ru.pr2.processing;

import ru.pr2.model.SupplierProduct;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvSupplierParser implements SupplierFileParser {
    private static final List<String> EXPECTED_HEADER = List.of(
            "supplierId", "supplierName", "product", "price", "quantity");

    private final NioTextReader reader;

    public CsvSupplierParser(NioTextReader reader) {
        this.reader = reader;
    }

    @Override
    public List<SupplierProduct> parse(Path path) throws IOException {
        List<String> lines = reader.read(path).lines()
                .filter(line -> !line.isBlank())
                .toList();
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("CSV-файл пуст: " + path.getFileName());
        }

        List<String> header = parseLine(removeBom(lines.getFirst()));
        if (!header.equals(EXPECTED_HEADER)) {
            throw new IllegalArgumentException("Некорректный CSV-заголовок: " + header);
        }

        List<SupplierProduct> products = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            List<String> fields = parseLine(lines.get(index));
            if (fields.size() != EXPECTED_HEADER.size()) {
                throw new IllegalArgumentException(
                        "В строке " + (index + 1) + " ожидалось 5 полей, получено " + fields.size());
            }
            try {
                products.add(new SupplierProduct(
                        Long.parseLong(fields.get(0).trim()),
                        fields.get(1),
                        fields.get(2),
                        new BigDecimal(fields.get(3).trim()),
                        Integer.parseInt(fields.get(4).trim())));
            } catch (RuntimeException exception) {
                throw new IllegalArgumentException(
                        "Ошибка данных в CSV-строке " + (index + 1), exception);
            }
        }
        if (products.isEmpty()) {
            throw new IllegalArgumentException("CSV не содержит записей: " + path.getFileName());
        }
        return List.copyOf(products);
    }

    static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;

        for (int index = 0; index < line.length(); index++) {
            char current = line.charAt(index);
            if (current == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    field.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (current == ',' && !quoted) {
                fields.add(field.toString().trim());
                field.setLength(0);
            } else {
                field.append(current);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Незакрытая кавычка в CSV: " + line);
        }
        fields.add(field.toString().trim());
        return List.copyOf(fields);
    }

    private String removeBom(String value) {
        return value.startsWith("\uFEFF") ? value.substring(1) : value;
    }
}
