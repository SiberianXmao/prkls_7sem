package ru.pr4.model;

import java.math.BigDecimal;
import java.util.Objects;

public record PriceUpdate(long productId, BigDecimal newPrice) {
    public PriceUpdate {
        if (productId <= 0) {
            throw new IllegalArgumentException("productId должен быть положительным");
        }
        Objects.requireNonNull(newPrice, "newPrice");
        if (newPrice.signum() < 0) {
            throw new IllegalArgumentException("newPrice не может быть отрицательной");
        }
    }
}
