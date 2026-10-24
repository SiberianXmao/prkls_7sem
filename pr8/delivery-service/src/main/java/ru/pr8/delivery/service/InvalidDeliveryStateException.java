package ru.pr8.delivery.service;

public class InvalidDeliveryStateException extends RuntimeException {
    public InvalidDeliveryStateException(String message) {
        super(message);
    }
}
