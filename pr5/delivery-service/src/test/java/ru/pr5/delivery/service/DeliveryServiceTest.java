package ru.pr5.delivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pr5.delivery.api.CreateDeliveryRequest;
import ru.pr5.delivery.client.OrderServiceClient;
import ru.pr5.delivery.client.OrderSnapshot;
import ru.pr5.delivery.domain.Delivery;
import ru.pr5.delivery.domain.DeliveryStatus;
import ru.pr5.delivery.repository.DeliveryRepository;
import ru.pr5.delivery.support.InstanceInfo;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-21T10:00:00Z");

    @Mock
    private DeliveryRepository repository;
    @Mock
    private OrderServiceClient orderServiceClient;

    private DeliveryService service;

    @BeforeEach
    void setUp() {
        service = new DeliveryService(
                repository,
                orderServiceClient,
                new InstanceInfo("delivery-test-1"),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createsDeliveryAfterReceivingOrderFromRemoteService() {
        when(repository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(orderServiceClient.findById(10L)).thenReturn(order("NEW"));
        when(repository.saveAndFlush(any(Delivery.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateDeliveryRequest(10L, "  Алексей  "));

        assertThat(response.orderId()).isEqualTo(10L);
        assertThat(response.courierName()).isEqualTo("Алексей");
        assertThat(response.status()).isEqualTo(DeliveryStatus.CREATED);
        assertThat(response.estimatedDeliveryAt()).isEqualTo(NOW.plusSeconds(172800));
        assertThat(response.servedBy()).isEqualTo("delivery-test-1");
        assertThat(response.orderServedBy()).isEqualTo("order-test-1");
        verify(orderServiceClient).findById(10L);
    }

    @Test
    void rejectsDeliveryForCancelledOrder() {
        when(repository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(orderServiceClient.findById(10L)).thenReturn(order("CANCELLED"));

        assertThatThrownBy(() -> service.create(new CreateDeliveryRequest(10L, "Алексей")))
                .isInstanceOf(InvalidDeliveryStateException.class)
                .hasMessageContaining("отменённого");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void checksDuplicateBeforeRemoteCall() {
        Delivery existing = Delivery.create(
                10L, "Иван", "Москва", "Алексей",
                NOW.plusSeconds(172800), NOW);
        when(repository.findByOrderId(10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(new CreateDeliveryRequest(10L, "Алексей")))
                .isInstanceOf(DuplicateDeliveryException.class);
        verify(orderServiceClient, never()).findById(10L);
    }

    private OrderSnapshot order(String status) {
        return new OrderSnapshot(
                10L,
                "Иван Петров",
                "Москва",
                "Ноутбук",
                1,
                new BigDecimal("89990.00"),
                status,
                NOW,
                NOW,
                "order-test-1");
    }
}
