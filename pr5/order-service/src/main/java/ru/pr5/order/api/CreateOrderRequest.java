package ru.pr5.order.api;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank @Size(max = 120) String customerName,
        @NotBlank @Size(max = 300) String deliveryAddress,
        @NotBlank @Size(max = 160) String productName,
        @Positive int quantity,
        @NotNull @DecimalMin("0.01") BigDecimal totalAmount) {
}
