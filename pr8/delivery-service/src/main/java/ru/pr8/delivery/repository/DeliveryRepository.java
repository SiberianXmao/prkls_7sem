package ru.pr8.delivery.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.pr8.delivery.domain.Delivery;

import java.util.UUID;

public interface DeliveryRepository extends ReactiveCrudRepository<Delivery, Long> {
    Mono<Delivery> findByOrderId(Long orderId);
    Mono<Boolean> existsByOrderId(Long orderId);
    Mono<Boolean> existsBySourceEventId(UUID sourceEventId);
}
