package ru.pr3.model;

public record BackpressureReport(int produced, int consumed, int dropped, int bufferCapacity) {
    public BackpressureReport {
        if (produced < 0 || consumed < 0 || dropped < 0 || bufferCapacity <= 0) {
            throw new IllegalArgumentException("Некорректная статистика backpressure");
        }
    }
}
