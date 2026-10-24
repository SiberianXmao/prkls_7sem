package ru.pr5.order.service;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(long orderId) {
        super("Заказ с ID %d не найден".formatted(orderId));
    }
}
