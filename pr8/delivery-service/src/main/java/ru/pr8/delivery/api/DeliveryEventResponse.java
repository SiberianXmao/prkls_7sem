package ru.pr8.delivery.api;

import ru.pr8.delivery.domain.DeliveryStatusHistory;

import java.time.Instant;

public record DeliveryEventResponse(Long orderId, String status, String requestId, Instant occurredAt) {
    public static DeliveryEventResponse from(DeliveryStatusHistory history) {
        return new DeliveryEventResponse(
                history.orderId(), history.status(), history.requestId(), history.occurredAt());
    }
}
