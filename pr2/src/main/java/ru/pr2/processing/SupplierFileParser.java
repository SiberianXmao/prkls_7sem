package ru.pr2.processing;

import ru.pr2.model.SupplierProduct;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface SupplierFileParser {
    List<SupplierProduct> parse(Path path) throws IOException;
}
