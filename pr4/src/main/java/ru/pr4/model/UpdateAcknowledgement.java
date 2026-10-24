package ru.pr4.model;

import java.math.BigDecimal;

public record UpdateAcknowledgement(
        long productId,
        BigDecimal oldPrice,
        BigDecimal newPrice,
        String status) {
}
