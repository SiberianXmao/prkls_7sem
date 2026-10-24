package ru.pr7.delivery.api;

import ru.pr7.delivery.domain.Delivery;
import ru.pr7.delivery.domain.DeliveryStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record DeliveryResponse(
        long id,
        long orderId,
        String customerName,
        String deliveryAddress,
        BigDecimal price,
        String courier,
        DeliveryStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static DeliveryResponse from(Delivery delivery) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getCustomerName(),
                delivery.getDeliveryAddress(),
                delivery.getPrice(),
                delivery.getCourier(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt());
    }
}
