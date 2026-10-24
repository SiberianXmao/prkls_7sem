package ru.pr2.db;

import ru.pr2.model.SupplierProduct;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public final class PostgresSupplierRepository implements SupplierRepository {
    private static final String UPSERT_PRODUCT = """
            INSERT INTO supplier_products
                (supplier_id, supplier_name, product, price, quantity, source_file, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (supplier_id, product) DO UPDATE SET
                supplier_name = EXCLUDED.supplier_name,
                price = EXCLUDED.price,
                quantity = EXCLUDED.quantity,
                source_file = EXCLUDED.source_file,
                updated_at = CURRENT_TIMESTAMP
            """;
    private static final String INSERT_LOG = """
            INSERT INTO processing_log (file_name, sha256, file_size, record_count)
            VALUES (?, ?, ?, ?)
            ON CONFLICT (file_name, sha256) DO NOTHING
            """;

    private final Connection connection;

    public PostgresSupplierRepository(String url, String user, String password) throws SQLException {
        this.connection = DriverManager.getConnection(url, user, password);
        initializeSchema();
    }

    private void initializeSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS supplier_products (
                        supplier_id BIGINT NOT NULL,
                        supplier_name VARCHAR(200) NOT NULL,
                        product VARCHAR(200) NOT NULL,
                        price NUMERIC(14, 2) NOT NULL CHECK (price >= 0),
                        quantity INTEGER NOT NULL CHECK (quantity >= 0),
                        source_file VARCHAR(255) NOT NULL,
                        updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (supplier_id, product)
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS processing_log (
                        id BIGSERIAL PRIMARY KEY,
                        file_name VARCHAR(255) NOT NULL,
                        sha256 CHAR(64) NOT NULL,
                        file_size BIGINT NOT NULL,
                        record_count INTEGER NOT NULL,
                        processed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE (file_name, sha256)
                    )
                    """);
        }
    }

    @Override
    public synchronized void saveProcessedFile(
            String fileName,
            long fileSize,
            String sha256,
            List<SupplierProduct> products) throws SQLException {
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (PreparedStatement productStatement = connection.prepareStatement(UPSERT_PRODUCT);
             PreparedStatement logStatement = connection.prepareStatement(INSERT_LOG)) {
            for (SupplierProduct product : products) {
                productStatement.setLong(1, product.supplierId());
                productStatement.setString(2, product.supplierName());
                productStatement.setString(3, product.product());
                productStatement.setBigDecimal(4, product.price());
                productStatement.setInt(5, product.quantity());
                productStatement.setString(6, fileName);
                productStatement.addBatch();
            }
            productStatement.executeBatch();

            logStatement.setString(1, fileName);
            logStatement.setString(2, sha256);
            logStatement.setLong(3, fileSize);
            logStatement.setInt(4, products.size());
            logStatement.executeUpdate();
            connection.commit();
        } catch (SQLException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    @Override
    public synchronized long productCount() throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM supplier_products")) {
            result.next();
            return result.getLong(1);
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
