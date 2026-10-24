package ru.pr4.model;

public record ProductStreamRequest(int limit, long delayMillis) {
    public ProductStreamRequest {
        if (limit <= 0 || delayMillis < 0 || delayMillis > 5_000) {
            throw new IllegalArgumentException("Некорректные параметры потока");
        }
    }
}
