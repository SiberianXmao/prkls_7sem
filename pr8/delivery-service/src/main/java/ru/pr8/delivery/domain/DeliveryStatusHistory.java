package ru.pr8.delivery.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("delivery_status_history")
public record DeliveryStatusHistory(
        @Id Long id,
        Long orderId,
        String status,
        String requestId,
        Instant occurredAt) {
}
