package ru.pr7.delivery.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import ru.pr7.delivery.api.DeliveryEvent;
import ru.pr7.delivery.api.DeliveryResponse;

import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeliveryEventPublisher {
    private final Map<Long, Sinks.Many<DeliveryEvent>> streams = new ConcurrentHashMap<>();
    private final Clock clock;

    public DeliveryEventPublisher(Clock clock) {
        this.clock = clock;
    }

    public DeliveryEvent snapshot(DeliveryResponse delivery) {
        return event(delivery, "SNAPSHOT");
    }

    public void publish(DeliveryResponse delivery, String type) {
        Sinks.Many<DeliveryEvent> sink = streams.get(delivery.id());
        if (sink != null) {
            sink.tryEmitNext(event(delivery, type));
        }
    }

    public void publishDeleted(DeliveryResponse delivery) {
        Sinks.Many<DeliveryEvent> sink = streams.remove(delivery.id());
        if (sink != null) {
            sink.tryEmitNext(event(delivery, "DELETED"));
            sink.tryEmitComplete();
        }
    }

    public Flux<DeliveryEvent> stream(long deliveryId) {
        return streams.computeIfAbsent(
                        deliveryId,
                        ignored -> Sinks.many().multicast().directBestEffort())
                .asFlux()
                .onBackpressureLatest();
    }

    private DeliveryEvent event(DeliveryResponse delivery, String type) {
        return new DeliveryEvent(
                delivery.id(),
                delivery.orderId(),
                delivery.status(),
                type,
                clock.instant());
    }
}
