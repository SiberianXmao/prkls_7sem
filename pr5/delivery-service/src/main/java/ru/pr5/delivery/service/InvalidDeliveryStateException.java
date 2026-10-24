package ru.pr5.delivery.service;

public class InvalidDeliveryStateException extends RuntimeException {
    public InvalidDeliveryStateException(String message) {
        super(message);
    }
}
