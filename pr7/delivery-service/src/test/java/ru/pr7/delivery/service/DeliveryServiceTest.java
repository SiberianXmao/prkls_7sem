package ru.pr7.delivery.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.pr7.delivery.api.CreateDeliveryRequest;
import ru.pr7.delivery.api.DeliveryResponse;
import ru.pr7.delivery.api.UpdateDeliveryRequest;
import ru.pr7.delivery.client.OrderServiceClient;
import ru.pr7.delivery.client.OrderSnapshot;
import ru.pr7.delivery.domain.Delivery;
import ru.pr7.delivery.domain.DeliveryStatus;
import ru.pr7.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {
    private static final Instant NOW = Instant.parse("2026-10-21T12:00:00Z");

    private DeliveryRepository repository;
    private OrderServiceClient orderServiceClient;
    private DeliveryEventPublisher eventPublisher;
    private DeliveryService service;

    @BeforeEach
    void setUp() {
        repository = mock(DeliveryRepository.class);
        orderServiceClient = mock(OrderServiceClient.class);
        eventPublisher = mock(DeliveryEventPublisher.class);
        Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
        service = new DeliveryService(repository, orderServiceClient, eventPublisher, clock);
    }

    @Test
    void createsDeliveryAfterReactiveOrderLookup() {
        OrderSnapshot order = new OrderSnapshot(
                1L,
                "Анна Смирнова",
                "Москва",
                new BigDecimal("2490.00"),
                "CONFIRMED");
        when(orderServiceClient.findById(1L)).thenReturn(Mono.just(order));
        when(repository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            saved.setId(10L);
            return Mono.just(saved);
        });

        StepVerifier.create(service.create(new CreateDeliveryRequest(1L, "Иван Курьер")))
                .assertNext(delivery -> {
                    assertThat(delivery.id()).isEqualTo(10L);
                    assertThat(delivery.customerName()).isEqualTo("Анна Смирнова");
                    assertThat(delivery.status()).isEqualTo(DeliveryStatus.CREATED);
                })
                .verifyComplete();

        ArgumentCaptor<DeliveryResponse> captor = ArgumentCaptor.forClass(DeliveryResponse.class);
        verify(eventPublisher).publish(captor.capture(), org.mockito.ArgumentMatchers.eq("CREATED"));
        assertThat(captor.getValue().orderId()).isEqualTo(1L);
    }

    @Test
    void filtersFluxByStatusAndPrice() {
        when(repository.findAllOrdered()).thenReturn(Flux.just(
                delivery(1L, DeliveryStatus.CREATED, "1000.00"),
                delivery(2L, DeliveryStatus.DELIVERED, "3000.00"),
                delivery(3L, DeliveryStatus.DELIVERED, "1500.00")));

        StepVerifier.create(service.findAll(
                        DeliveryStatus.DELIVERED,
                        new BigDecimal("2000.00")))
                .expectNextMatches(delivery -> delivery.id() == 2L)
                .verifyComplete();
    }

    @Test
    void rejectsInvalidStatusTransition() {
        when(repository.findById(1L)).thenReturn(Mono.just(
                delivery(1L, DeliveryStatus.CREATED, "2490.00")));

        StepVerifier.create(service.update(
                        1L,
                        new UpdateDeliveryRequest("Иван Курьер", DeliveryStatus.DELIVERED)))
                .expectError(InvalidDeliveryStateException.class)
                .verify();
    }

    @Test
    void reportsMissingDeliveryThroughReactiveChain() {
        when(repository.findById(404L)).thenReturn(Mono.empty());

        StepVerifier.create(service.findById(404L))
                .expectError(DeliveryNotFoundException.class)
                .verify();
    }

    private Delivery delivery(long id, DeliveryStatus status, String price) {
        return new Delivery(
                id,
                1L,
                "Клиент",
                "Москва",
                new BigDecimal(price),
                "Курьер",
                status,
                NOW,
                NOW);
    }
}
