package ru.pr5.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pr5.order.domain.CustomerOrder;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {
}
