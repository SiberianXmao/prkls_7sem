package ru.pr8.order.api;

import ru.pr8.order.domain.CustomerOrder;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String clientRequestId,
        Long productId,
        String productName,
        int quantity,
        BigDecimal totalPrice,
        String customerName,
        String deliveryAddress,
        String status,
        Instant createdAt,
        String servedBy) {
    public static OrderResponse from(CustomerOrder order, String instanceId) {
        return new OrderResponse(
                order.getId(),
                order.getClientRequestId(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalPrice(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getStatus().name(),
                order.getCreatedAt(),
                instanceId);
    }
}
