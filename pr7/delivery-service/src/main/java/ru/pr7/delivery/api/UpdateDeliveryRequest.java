package ru.pr7.delivery.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.pr7.delivery.domain.DeliveryStatus;

public record UpdateDeliveryRequest(
        @NotBlank(message = "courier обязателен") String courier,
        @NotNull(message = "status обязателен") DeliveryStatus status) {
}
