package ru.pr8.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pr8.order.domain.CustomerOrder;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
    Optional<CustomerOrder> findByClientRequestId(String clientRequestId);
}
