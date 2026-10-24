package ru.pr5.delivery.service;

public class RelatedOrderNotFoundException extends RuntimeException {
    public RelatedOrderNotFoundException(long orderId) {
        super("Нельзя создать доставку: заказ с ID %d не найден".formatted(orderId));
    }
}
