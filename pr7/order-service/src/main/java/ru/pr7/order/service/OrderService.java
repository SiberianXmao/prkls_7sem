package ru.pr7.order.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr7.order.api.OrderResponse;
import ru.pr7.order.repository.CustomerOrderRepository;

@Service
public class OrderService {
    private final CustomerOrderRepository repository;

    public OrderService(CustomerOrderRepository repository) {
        this.repository = repository;
    }

    public Mono<OrderResponse> findById(long orderId) {
        return repository.findById(orderId)
                .map(OrderResponse::from);
    }

    public Flux<OrderResponse> findAll() {
        return repository.findAll()
                .map(OrderResponse::from);
    }
}
