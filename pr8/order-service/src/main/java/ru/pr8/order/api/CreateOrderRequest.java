package ru.pr8.order.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotBlank String clientRequestId,
        @Positive long productId,
        @Positive int quantity,
        @NotBlank String customerName,
        @NotBlank String deliveryAddress) {
}
