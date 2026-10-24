package ru.pr5.delivery.api;

import java.time.Instant;

public record InstanceResponse(String service, String servedBy, Instant timestamp) {
}
