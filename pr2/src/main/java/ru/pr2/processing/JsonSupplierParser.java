package ru.pr2.processing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.pr2.model.SupplierProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class JsonSupplierParser implements SupplierFileParser {
    private static final TypeReference<List<SupplierProduct>> PRODUCT_LIST = new TypeReference<>() {
    };

    private final NioTextReader reader;
    private final ObjectMapper objectMapper;

    public JsonSupplierParser(NioTextReader reader, ObjectMapper objectMapper) {
        this.reader = reader;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<SupplierProduct> parse(Path path) throws IOException {
        List<SupplierProduct> products = objectMapper.readValue(reader.read(path), PRODUCT_LIST);
        if (products.isEmpty()) {
            throw new IllegalArgumentException("JSON не содержит записей: " + path.getFileName());
        }
        return List.copyOf(products);
    }
}
