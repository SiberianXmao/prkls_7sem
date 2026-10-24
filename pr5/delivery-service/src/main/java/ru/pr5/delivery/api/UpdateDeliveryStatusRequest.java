package ru.pr5.delivery.api;

import jakarta.validation.constraints.NotNull;
import ru.pr5.delivery.domain.DeliveryStatus;

public record UpdateDeliveryStatusRequest(@NotNull DeliveryStatus status) {
}
