package ru.pr4.model;

public record ProductRequest(long productId) {
    public ProductRequest {
        if (productId <= 0) {
            throw new IllegalArgumentException("productId должен быть положительным");
        }
    }
}
