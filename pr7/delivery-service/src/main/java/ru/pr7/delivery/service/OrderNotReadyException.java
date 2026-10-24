package ru.pr7.delivery.service;

public class OrderNotReadyException extends RuntimeException {
    public OrderNotReadyException(long orderId) {
        super("Заказ " + orderId + " не подтверждён и не может быть передан в доставку");
    }
}
