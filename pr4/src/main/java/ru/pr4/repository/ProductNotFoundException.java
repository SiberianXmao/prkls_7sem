package ru.pr4.repository;

public final class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(long productId) {
        super("Товар с ID " + productId + " не найден");
    }
}
