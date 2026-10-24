package ru.pr7.delivery.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateDeliveryRequest(
        @Positive(message = "orderId должен быть положительным") long orderId,
        @NotBlank(message = "courier обязателен") String courier) {
}
