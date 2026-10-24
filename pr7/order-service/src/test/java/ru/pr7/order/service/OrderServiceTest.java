package ru.pr7.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.pr7.order.domain.CustomerOrder;
import ru.pr7.order.domain.OrderStatus;
import ru.pr7.order.repository.CustomerOrderRepository;

import java.math.BigDecimal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    private CustomerOrderRepository repository;
    private OrderService service;

    @BeforeEach
    void setUp() {
        repository = mock(CustomerOrderRepository.class);
        service = new OrderService(repository);
    }

    @Test
    void returnsOrderAsMono() {
        when(repository.findById(1L)).thenReturn(Mono.just(order(1L, "Анна")));

        StepVerifier.create(service.findById(1L))
                .expectNextMatches(order -> order.id() == 1L
                        && order.customerName().equals("Анна")
                        && order.status() == OrderStatus.CONFIRMED)
                .verifyComplete();
    }

    @Test
    void returnsOrdersAsFlux() {
        when(repository.findAll()).thenReturn(Flux.just(
                order(1L, "Анна"),
                order(2L, "Иван")));

        StepVerifier.create(service.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    private CustomerOrder order(long id, String customer) {
        return new CustomerOrder(
                id,
                customer,
                "Москва",
                new BigDecimal("2490.00"),
                OrderStatus.CONFIRMED);
    }
}
