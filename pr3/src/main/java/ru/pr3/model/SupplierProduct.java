package ru.pr3.model;

import java.math.BigDecimal;

public record SupplierProduct(
        long supplierId,
        String supplierName,
        String product,
        BigDecimal price,
        int quantity) {
}
