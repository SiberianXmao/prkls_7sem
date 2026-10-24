package ru.pr7.delivery.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.pr7.delivery.domain.Delivery;

public interface DeliveryRepository extends ReactiveCrudRepository<Delivery, Long> {
    @Query("""
            SELECT id, order_id, customer_name, delivery_address, price, courier,
                   status, created_at, updated_at
            FROM deliveries
            ORDER BY id
            """)
    Flux<Delivery> findAllOrdered();
}
