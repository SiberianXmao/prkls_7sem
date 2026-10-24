package ru.pr7.delivery.client;

import java.math.BigDecimal;

public record OrderSnapshot(
        long id,
        String customerName,
        String deliveryAddress,
        BigDecimal totalAmount,
        String status) {
}
