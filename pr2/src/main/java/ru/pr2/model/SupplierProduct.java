package ru.pr2.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SupplierProduct(
        long supplierId,
        String supplierName,
        String product,
        BigDecimal price,
        int quantity) {

    public SupplierProduct {
        if (supplierId <= 0) {
            throw new IllegalArgumentException("supplierId должен быть положительным");
        }
        supplierName = requireText(supplierName, "supplierName");
        product = requireText(product, "product");
        Objects.requireNonNull(price, "price");
        if (price.signum() < 0) {
            throw new IllegalArgumentException("price не может быть отрицательной");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity не может быть отрицательным");
        }
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field);
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " не может быть пустым");
        }
        return normalized;
    }
}
