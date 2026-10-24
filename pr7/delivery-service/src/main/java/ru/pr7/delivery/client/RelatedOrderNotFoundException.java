package ru.pr7.delivery.client;

public class RelatedOrderNotFoundException extends RuntimeException {
    public RelatedOrderNotFoundException(long orderId) {
        super("Заказ с ID " + orderId + " не найден");
    }
}
