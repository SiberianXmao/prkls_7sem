package ru.pr8.order.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.pr8.order.api.CreateOrderRequest;
import ru.pr8.order.client.ProductSnapshot;
import ru.pr8.order.client.SupplierCatalogGateway;
import ru.pr8.order.domain.CustomerOrder;
import ru.pr8.order.event.OrderEventPublisher;
import ru.pr8.order.repository.CustomerOrderRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    private final CustomerOrderRepository repository = mock(CustomerOrderRepository.class);
    private final SupplierCatalogGateway catalog = mock(SupplierCatalogGateway.class);
    private final OrderEventPublisher publisher = mock(OrderEventPublisher.class);
    private final OrderService service = new OrderService(repository, catalog, publisher);

    @Test
    void createsOrderAndPublishesEvent() {
        var request = new CreateOrderRequest("req-1", 10L, 2, "Anna", "Moscow");
        when(repository.findByClientRequestId("req-1")).thenReturn(Optional.empty());
        when(catalog.requireAvailable(10L, 2)).thenReturn(
                new ProductSnapshot(10L, 1L, "Monitor", new BigDecimal("100.00"), 2, 5));
        when(repository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.create(request, "trace-1");

        assertThat(result.totalPrice()).isEqualByComparingTo("200.00");
        verify(publisher).publish(any(CustomerOrder.class));
    }

    @Test
    void repeatedClientRequestIsIdempotent() {
        var request = new CreateOrderRequest("req-1", 10L, 2, "Anna", "Moscow");
        CustomerOrder existing = new CustomerOrder(
                "req-1", 10L, "Monitor", new BigDecimal("100.00"), 2, "Anna", "Moscow", "trace-1");
        when(repository.findByClientRequestId("req-1")).thenReturn(Optional.of(existing));

        service.create(request, "trace-2");

        verify(catalog, never()).requireAvailable(10L, 2);
        verify(publisher, never()).publish(any());
    }
}
