package ru.pr4.model;

import java.util.Objects;

public record ViewEvent(long productId, String clientId) {
    public ViewEvent {
        if (productId <= 0) {
            throw new IllegalArgumentException("productId должен быть положительным");
        }
        Objects.requireNonNull(clientId, "clientId");
        if (clientId.isBlank()) {
            throw new IllegalArgumentException("clientId не может быть пустым");
        }
    }
}
