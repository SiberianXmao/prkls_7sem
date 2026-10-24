package ru.pr8.delivery.api;

import ru.pr8.delivery.domain.Delivery;

import java.math.BigDecimal;
import java.time.Instant;

public record DeliveryResponse(
        Long id,
        Long orderId,
        String customerName,
        String deliveryAddress,
        BigDecimal orderTotal,
        String courier,
        String status,
        Instant createdAt,
        Instant updatedAt,
        String servedBy) {
    public static DeliveryResponse from(Delivery delivery, String instanceId) {
        return new DeliveryResponse(
                delivery.id(),
                delivery.orderId(),
                delivery.customerName(),
                delivery.deliveryAddress(),
                delivery.orderTotal(),
                delivery.courier(),
                delivery.status().name(),
                delivery.createdAt(),
                delivery.updatedAt(),
                instanceId);
    }
}
