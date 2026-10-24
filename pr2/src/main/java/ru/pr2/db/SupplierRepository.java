package ru.pr2.db;

import ru.pr2.model.SupplierProduct;

import java.sql.SQLException;
import java.util.List;

public interface SupplierRepository extends AutoCloseable {
    void saveProcessedFile(
            String fileName,
            long fileSize,
            String sha256,
            List<SupplierProduct> products) throws SQLException;

    long productCount() throws SQLException;

    @Override
    default void close() throws SQLException {
    }
}
