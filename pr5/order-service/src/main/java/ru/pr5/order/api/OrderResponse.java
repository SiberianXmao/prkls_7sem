package ru.pr5.order.api;

import ru.pr5.order.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String customerName,
        String deliveryAddress,
        String productName,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt,
        Instant updatedAt,
        String servedBy) {
}
