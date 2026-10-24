package ru.pr4.model;

import java.math.BigDecimal;
import java.util.Objects;

public record SupplierProduct(
        long productId,
        long supplierId,
        String supplierName,
        String product,
        BigDecimal price,
        int quantity) {

    public SupplierProduct {
        if (productId <= 0 || supplierId <= 0) {
            throw new IllegalArgumentException("ID должны быть положительными");
        }
        supplierName = requireText(supplierName, "supplierName");
        product = requireText(product, "product");
        Objects.requireNonNull(price, "price");
        if (price.signum() < 0 || quantity < 0) {
            throw new IllegalArgumentException("Цена и количество не могут быть отрицательными");
        }
    }

    public SupplierProduct withPrice(BigDecimal newPrice) {
        return new SupplierProduct(
                productId, supplierId, supplierName, product, newPrice, quantity);
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
