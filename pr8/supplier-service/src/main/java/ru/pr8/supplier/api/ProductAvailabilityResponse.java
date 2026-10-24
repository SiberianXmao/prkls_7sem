package ru.pr8.supplier.api;

import java.math.BigDecimal;

public record ProductAvailabilityResponse(
        Long productId,
        Long supplierId,
        String productName,
        BigDecimal unitPrice,
        int requestedQuantity,
        int availableQuantity) {
}
