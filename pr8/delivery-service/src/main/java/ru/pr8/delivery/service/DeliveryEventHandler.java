package ru.pr8.delivery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr8.delivery.api.DeliveryEventResponse;
import ru.pr8.delivery.domain.Delivery;
import ru.pr8.delivery.domain.DeliveryStatus;
import ru.pr8.delivery.domain.DeliveryStatusHistory;
import ru.pr8.delivery.event.OrderCreatedEvent;
import ru.pr8.delivery.repository.DeliveryRepository;
import ru.pr8.delivery.repository.DeliveryStatusHistoryRepository;

import java.time.Instant;
import java.util.List;

@Service
public class DeliveryEventHandler {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryStatusHistoryRepository historyRepository;
    private final DeliveryTimelinePublisher timelinePublisher;
    private final String defaultCourier;

    public DeliveryEventHandler(
            DeliveryRepository deliveryRepository,
            DeliveryStatusHistoryRepository historyRepository,
            DeliveryTimelinePublisher timelinePublisher,
            @Value("${delivery.default-courier:NOT_ASSIGNED}") String defaultCourier) {
        this.deliveryRepository = deliveryRepository;
        this.historyRepository = historyRepository;
        this.timelinePublisher = timelinePublisher;
        this.defaultCourier = defaultCourier;
    }

    public Mono<Void> handle(OrderCreatedEvent event) {
        return deliveryRepository.existsBySourceEventId(event.eventId())
                .flatMap(exists -> exists ? Mono.empty() : createDelivery(event))
                .onErrorResume(DuplicateKeyException.class, ignored -> Mono.empty());
    }

    private Mono<Void> createDelivery(OrderCreatedEvent event) {
        Instant now = Instant.now();
        Delivery delivery = new Delivery(
                null,
                event.eventId(),
                event.orderId(),
                event.customerName(),
                event.deliveryAddress(),
                event.totalPrice(),
                defaultCourier,
                DeliveryStatus.DELIVERY_CREATED,
                event.requestId(),
                now,
                now);
        return deliveryRepository.save(delivery)
                .flatMapMany(saved -> Flux.fromIterable(List.of("CREATED", "CONFIRMED", "DELIVERY_CREATED")))
                .concatMap(status -> appendStatus(event.orderId(), status, event.requestId()))
                .then();
    }

    private Mono<DeliveryStatusHistory> appendStatus(long orderId, String status, String requestId) {
        DeliveryStatusHistory history = new DeliveryStatusHistory(null, orderId, status, requestId, Instant.now());
        return historyRepository.save(history)
                .doOnNext(saved -> timelinePublisher.publish(DeliveryEventResponse.from(saved)));
    }
}
