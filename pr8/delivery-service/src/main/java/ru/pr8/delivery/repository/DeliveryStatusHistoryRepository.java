package ru.pr8.delivery.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.pr8.delivery.domain.DeliveryStatusHistory;

public interface DeliveryStatusHistoryRepository extends ReactiveCrudRepository<DeliveryStatusHistory, Long> {
    Flux<DeliveryStatusHistory> findByOrderIdOrderByIdAsc(Long orderId);
}
