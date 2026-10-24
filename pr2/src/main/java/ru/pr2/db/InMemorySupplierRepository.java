package ru.pr2.db;

import ru.pr2.model.SupplierProduct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemorySupplierRepository implements SupplierRepository {
    private final Map<ProductKey, SupplierProduct> products = new LinkedHashMap<>();

    @Override
    public synchronized void saveProcessedFile(
            String fileName,
            long fileSize,
            String sha256,
            List<SupplierProduct> values) {
        values.forEach(value -> products.put(
                new ProductKey(value.supplierId(), value.product()), value));
    }

    @Override
    public synchronized long productCount() {
        return products.size();
    }

    public synchronized List<SupplierProduct> products() {
        return List.copyOf(products.values());
    }

    private record ProductKey(long supplierId, String product) {
    }
}
