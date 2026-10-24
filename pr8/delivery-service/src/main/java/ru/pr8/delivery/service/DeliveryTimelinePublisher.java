package ru.pr8.delivery.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import ru.pr8.delivery.api.DeliveryEventResponse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class DeliveryTimelinePublisher {
    private final ConcurrentMap<Long, Sinks.Many<DeliveryEventResponse>> sinks = new ConcurrentHashMap<>();

    public void publish(DeliveryEventResponse event) {
        sink(event.orderId()).tryEmitNext(event);
    }

    public Flux<DeliveryEventResponse> stream(long orderId) {
        return sink(orderId).asFlux().onBackpressureLatest();
    }

    private Sinks.Many<DeliveryEventResponse> sink(long orderId) {
        return sinks.computeIfAbsent(orderId, ignored -> Sinks.many().multicast().directBestEffort());
    }
}
