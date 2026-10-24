package ru.pr8.order.api;

import java.time.Instant;

public record ApiError(Instant timestamp, int status, String message, String path) {
}
