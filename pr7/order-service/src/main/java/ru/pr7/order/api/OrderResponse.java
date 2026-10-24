package ru.pr7.order.api;

import ru.pr7.order.domain.CustomerOrder;
import ru.pr7.order.domain.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(
        long id,
        String customerName,
        String deliveryAddress,
        BigDecimal totalAmount,
        OrderStatus status) {

    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getTotalAmount(),
                order.getStatus());
    }
}
