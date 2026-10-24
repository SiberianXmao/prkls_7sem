package ru.pr5.order.api;

import jakarta.validation.constraints.NotNull;
import ru.pr5.order.domain.OrderStatus;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
}
