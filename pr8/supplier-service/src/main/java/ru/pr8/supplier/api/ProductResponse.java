package ru.pr8.supplier.api;

import ru.pr8.supplier.domain.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long supplierId,
        String sku,
        String name,
        BigDecimal price,
        int availableQuantity) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSupplierId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                product.getAvailableQuantity());
    }
}
