package ru.pr8.order.client;

public class SupplierUnavailableException extends RuntimeException {
    public SupplierUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
