package ru.pr5.delivery.client;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderSnapshot(
        Long id,
        String customerName,
        String deliveryAddress,
        String productName,
        int quantity,
        BigDecimal totalAmount,
        String status,
        Instant createdAt,
        Instant updatedAt,
        String servedBy) {
}
