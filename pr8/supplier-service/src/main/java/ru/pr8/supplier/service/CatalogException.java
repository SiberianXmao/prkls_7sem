package ru.pr8.supplier.service;

public class CatalogException extends RuntimeException {
    private final int status;

    public CatalogException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
