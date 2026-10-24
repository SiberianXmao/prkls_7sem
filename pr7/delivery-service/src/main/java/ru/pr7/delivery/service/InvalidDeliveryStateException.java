package ru.pr7.delivery.service;

import ru.pr7.delivery.domain.DeliveryStatus;

public class InvalidDeliveryStateException extends RuntimeException {
    public InvalidDeliveryStateException(DeliveryStatus current, DeliveryStatus requested) {
        super("Недопустимый переход состояния: " + current + " -> " + requested);
    }
}
