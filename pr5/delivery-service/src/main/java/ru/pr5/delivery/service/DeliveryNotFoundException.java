package ru.pr5.delivery.service;

public class DeliveryNotFoundException extends RuntimeException {
    public DeliveryNotFoundException(long deliveryId) {
        super("Доставка с ID %d не найдена".formatted(deliveryId));
    }
}
