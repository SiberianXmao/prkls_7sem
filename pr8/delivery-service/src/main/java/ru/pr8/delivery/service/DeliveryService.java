package ru.pr8.delivery.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr8.delivery.api.DeliveryEventResponse;
import ru.pr8.delivery.api.DeliveryResponse;
import ru.pr8.delivery.domain.Delivery;
import ru.pr8.delivery.domain.DeliveryStatus;
import ru.pr8.delivery.domain.DeliveryStatusHistory;
import ru.pr8.delivery.repository.DeliveryRepository;
import ru.pr8.delivery.repository.DeliveryStatusHistoryRepository;

import java.time.Instant;

@Service
public class DeliveryService {
    private final DeliveryRepository repository;
    private final DeliveryStatusHistoryRepository historyRepository;
    private final DeliveryTimelinePublisher timelinePublisher;
    private final String instanceId;

    public DeliveryService(
            DeliveryRepository repository,
            DeliveryStatusHistoryRepository historyRepository,
            DeliveryTimelinePublisher timelinePublisher) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.timelinePublisher = timelinePublisher;
        this.instanceId = System.getenv().getOrDefault("HOSTNAME", "local");
    }

    public Flux<DeliveryResponse> findAll() {
        return repository.findAll().map(delivery -> DeliveryResponse.from(delivery, instanceId));
    }

    public Mono<DeliveryResponse> findById(long id) {
        return requireDelivery(id).map(delivery -> DeliveryResponse.from(delivery, instanceId));
    }

    public Mono<DeliveryResponse> updateStatus(long id, DeliveryStatus requestedStatus) {
        return requireDelivery(id)
                .flatMap(delivery -> validateTransition(delivery, requestedStatus))
                .map(delivery -> delivery.withStatus(requestedStatus))
                .flatMap(repository::save)
                .flatMap(saved -> appendHistory(saved)
                        .thenReturn(DeliveryResponse.from(saved, instanceId)));
    }

    public Flux<DeliveryEventResponse> stream(long orderId) {
        return repository.existsByOrderId(orderId)
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new DeliveryNotFoundException("Delivery for order " + orderId + " not found"));
                    }
                    Flux<DeliveryEventResponse> history = historyRepository.findByOrderIdOrderByIdAsc(orderId)
                            .map(DeliveryEventResponse::from);
                    return Flux.concat(history, timelinePublisher.stream(orderId));
                });
    }

    private Mono<Delivery> requireDelivery(long id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new DeliveryNotFoundException("Delivery " + id + " not found")));
    }

    private Mono<Delivery> validateTransition(Delivery delivery, DeliveryStatus requested) {
        boolean allowed = switch (delivery.status()) {
            case DELIVERY_CREATED -> requested == DeliveryStatus.IN_TRANSIT || requested == DeliveryStatus.CANCELLED;
            case IN_TRANSIT -> requested == DeliveryStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
        if (!allowed) {
            return Mono.error(new InvalidDeliveryStateException(
                    "Invalid delivery transition: " + delivery.status() + " -> " + requested));
        }
        return Mono.just(delivery);
    }

    private Mono<DeliveryStatusHistory> appendHistory(Delivery delivery) {
        DeliveryStatusHistory history = new DeliveryStatusHistory(
                null,
                delivery.orderId(),
                delivery.status().name(),
                delivery.requestId(),
                Instant.now());
        return historyRepository.save(history)
                .doOnNext(saved -> timelinePublisher.publish(DeliveryEventResponse.from(saved)));
    }
}
