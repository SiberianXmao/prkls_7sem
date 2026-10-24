package ru.pr7.delivery.client;

public class RemoteOrderServiceException extends RuntimeException {
    public RemoteOrderServiceException(String message) {
        super(message);
    }

    public RemoteOrderServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
