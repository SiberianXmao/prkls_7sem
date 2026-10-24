package ru.pr7.delivery.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr7.delivery.api.CreateDeliveryRequest;
import ru.pr7.delivery.api.DeliveryEvent;
import ru.pr7.delivery.api.DeliveryResponse;
import ru.pr7.delivery.api.UpdateDeliveryRequest;
import ru.pr7.delivery.client.OrderServiceClient;
import ru.pr7.delivery.domain.Delivery;
import ru.pr7.delivery.domain.DeliveryStatus;
import ru.pr7.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

@Service
public class DeliveryService {
    private final DeliveryRepository repository;
    private final OrderServiceClient orderServiceClient;
    private final DeliveryEventPublisher eventPublisher;
    private final Clock clock;

    public DeliveryService(
            DeliveryRepository repository,
            OrderServiceClient orderServiceClient,
            DeliveryEventPublisher eventPublisher,
            Clock clock) {
        this.repository = repository;
        this.orderServiceClient = orderServiceClient;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    public Mono<DeliveryResponse> create(CreateDeliveryRequest request) {
        return orderServiceClient.findById(request.orderId())
                .filter(order -> "CONFIRMED".equals(order.status()))
                .switchIfEmpty(Mono.error(new OrderNotReadyException(request.orderId())))
                .flatMap(order -> {
                    Instant now = clock.instant();
                    Delivery delivery = new Delivery(
                            null,
                            order.id(),
                            order.customerName(),
                            order.deliveryAddress(),
                            order.totalAmount(),
                            request.courier().trim(),
                            DeliveryStatus.CREATED,
                            now,
                            now);
                    return repository.save(delivery);
                })
                .map(DeliveryResponse::from)
                .doOnNext(delivery -> eventPublisher.publish(delivery, "CREATED"));
    }

    public Mono<DeliveryResponse> findById(long deliveryId) {
        return findEntity(deliveryId)
                .map(DeliveryResponse::from);
    }

    public Flux<DeliveryResponse> findAll(DeliveryStatus status, BigDecimal minPrice) {
        return repository.findAllOrdered()
                .filter(delivery -> status == null || delivery.getStatus() == status)
                .filter(delivery -> minPrice == null || delivery.getPrice().compareTo(minPrice) >= 0)
                .map(DeliveryResponse::from);
    }

    public Mono<DeliveryResponse> update(long deliveryId, UpdateDeliveryRequest request) {
        return findEntity(deliveryId)
                .flatMap(delivery -> {
                    validateTransition(delivery.getStatus(), request.status());
                    delivery.setCourier(request.courier().trim());
                    delivery.setStatus(request.status());
                    delivery.setUpdatedAt(clock.instant());
                    return repository.save(delivery);
                })
                .map(DeliveryResponse::from)
                .doOnNext(delivery -> eventPublisher.publish(delivery, "UPDATED"));
    }

    public Mono<Void> delete(long deliveryId) {
        return findEntity(deliveryId)
                .flatMap(delivery -> repository.delete(delivery).thenReturn(delivery))
                .map(DeliveryResponse::from)
                .doOnNext(eventPublisher::publishDeleted)
                .then();
    }

    public Flux<DeliveryEvent> stream(long deliveryId) {
        return findEntity(deliveryId)
                .map(DeliveryResponse::from)
                .map(eventPublisher::snapshot)
                .flatMapMany(snapshot -> Flux.concat(
                        Mono.just(snapshot),
                        eventPublisher.stream(deliveryId)));
    }

    private Mono<Delivery> findEntity(long deliveryId) {
        return repository.findById(deliveryId)
                .switchIfEmpty(Mono.error(new DeliveryNotFoundException(deliveryId)));
    }

    private void validateTransition(DeliveryStatus current, DeliveryStatus requested) {
        if (current == requested) {
            return;
        }

        boolean allowed = switch (current) {
            case CREATED -> requested == DeliveryStatus.ACCEPTED
                    || requested == DeliveryStatus.CANCELLED;
            case ACCEPTED -> requested == DeliveryStatus.IN_TRANSIT
                    || requested == DeliveryStatus.CANCELLED;
            case IN_TRANSIT -> requested == DeliveryStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new InvalidDeliveryStateException(current, requested);
        }
    }
}
