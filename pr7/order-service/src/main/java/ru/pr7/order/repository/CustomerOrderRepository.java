package ru.pr7.order.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.pr7.order.domain.CustomerOrder;

public interface CustomerOrderRepository extends ReactiveCrudRepository<CustomerOrder, Long> {
}
