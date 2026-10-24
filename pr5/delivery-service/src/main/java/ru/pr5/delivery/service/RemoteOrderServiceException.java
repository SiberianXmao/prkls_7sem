package ru.pr5.delivery.service;

public class RemoteOrderServiceException extends RuntimeException {
    public RemoteOrderServiceException(String message) {
        super(message);
    }

    public RemoteOrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
