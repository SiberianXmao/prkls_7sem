package ru.pr7.delivery.service;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(long deliveryId) {
        super("Доставка с ID " + deliveryId + " не найдена");
    }
}
