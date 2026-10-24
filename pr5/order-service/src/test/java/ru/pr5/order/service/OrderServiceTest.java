package ru.pr5.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pr5.order.api.CreateOrderRequest;
import ru.pr5.order.domain.CustomerOrder;
import ru.pr5.order.domain.OrderStatus;
import ru.pr5.order.repository.CustomerOrderRepository;
import ru.pr5.order.support.InstanceInfo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-21T10:00:00Z");

    @Mock
    private CustomerOrderRepository repository;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(
                repository,
                new InstanceInfo("order-test-1"),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsAndNormalizesOrder() {
        when(repository.save(org.mockito.ArgumentMatchers.any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateOrderRequest(
                "  Иван Петров  ",
                "  Москва  ",
                "  Ноутбук  ",
                1,
                new BigDecimal("89990.00")));

        ArgumentCaptor<CustomerOrder> captor = ArgumentCaptor.forClass(CustomerOrder.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCustomerName()).isEqualTo("Иван Петров");
        assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(response.servedBy()).isEqualTo("order-test-1");
    }

    @Test
    void reportsMissingOrder() {
        when(repository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void doesNotAllowConfirmedOrderToReturnToNew() {
        CustomerOrder order = CustomerOrder.create(
                "Иван", "Москва", "Ноутбук", 1,
                new BigDecimal("100.00"), NOW);
        order.changeStatus(OrderStatus.CONFIRMED, NOW);
        when(repository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.changeStatus(1L, OrderStatus.NEW))
                .isInstanceOf(InvalidOrderStateException.class)
                .hasMessageContaining("нельзя вернуть");
    }
}
