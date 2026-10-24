package ru.pr8.delivery.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("deliveries")
public record Delivery(
        @Id Long id,
        UUID sourceEventId,
        Long orderId,
        String customerName,
        String deliveryAddress,
        BigDecimal orderTotal,
        String courier,
        DeliveryStatus status,
        String requestId,
        Instant createdAt,
        Instant updatedAt) {
    public Delivery withStatus(DeliveryStatus newStatus) {
        return new Delivery(
                id,
                sourceEventId,
                orderId,
                customerName,
                deliveryAddress,
                orderTotal,
                courier,
                newStatus,
                requestId,
                createdAt,
                Instant.now());
    }
}
