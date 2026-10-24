package ru.pr8.order.client;

import java.math.BigDecimal;

public record ProductSnapshot(
        Long productId,
        Long supplierId,
        String productName,
        BigDecimal unitPrice,
        int requestedQuantity,
        int availableQuantity) {
}
