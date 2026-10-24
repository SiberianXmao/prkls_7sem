package ru.pr5.delivery.api;

import ru.pr5.delivery.domain.DeliveryStatus;

import java.time.Instant;

public record DeliveryResponse(
        Long id,
        Long orderId,
        String customerName,
        String deliveryAddress,
        String courierName,
        DeliveryStatus status,
        Instant estimatedDeliveryAt,
        Instant createdAt,
        Instant updatedAt,
        String servedBy,
        String orderServedBy) {
}
