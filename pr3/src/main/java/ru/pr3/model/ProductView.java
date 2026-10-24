package ru.pr3.model;

import java.math.BigDecimal;

public record ProductView(
        long supplierId,
        String supplierName,
        String product,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalCost,
        String processedByThread) {
}
