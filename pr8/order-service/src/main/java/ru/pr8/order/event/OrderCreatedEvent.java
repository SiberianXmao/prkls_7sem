package ru.pr8.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        Long orderId,
        String customerName,
        String deliveryAddress,
        BigDecimal totalPrice,
        String requestId,
        Instant occurredAt) {
}
