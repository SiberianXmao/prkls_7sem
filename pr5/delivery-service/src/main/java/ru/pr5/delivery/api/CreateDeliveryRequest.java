package ru.pr5.delivery.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateDeliveryRequest(
        @NotNull @Positive Long orderId,
        @NotBlank @Size(max = 120) String courierName) {
}
