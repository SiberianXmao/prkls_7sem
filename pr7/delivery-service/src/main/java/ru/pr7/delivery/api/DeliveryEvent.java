package ru.pr7.delivery.api;

import ru.pr7.delivery.domain.DeliveryStatus;

import java.time.Instant;

public record DeliveryEvent(
        long deliveryId,
        long orderId,
        DeliveryStatus status,
        String type,
        Instant occurredAt) {
}
