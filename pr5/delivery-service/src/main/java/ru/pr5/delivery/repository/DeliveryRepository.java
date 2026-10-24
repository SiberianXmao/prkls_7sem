package ru.pr5.delivery.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pr5.delivery.domain.Delivery;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
}
