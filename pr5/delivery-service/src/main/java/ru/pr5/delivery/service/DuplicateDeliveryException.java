package ru.pr5.delivery.service;

public class DuplicateDeliveryException extends RuntimeException {
    public DuplicateDeliveryException(long orderId) {
        super("Для заказа %d доставка уже существует".formatted(orderId));
    }
}
