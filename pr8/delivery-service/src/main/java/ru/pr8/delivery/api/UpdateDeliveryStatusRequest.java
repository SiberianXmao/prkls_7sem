package ru.pr8.delivery.api;

import jakarta.validation.constraints.NotNull;
import ru.pr8.delivery.domain.DeliveryStatus;

public record UpdateDeliveryStatusRequest(@NotNull DeliveryStatus status) {
}
