package ru.pr8.delivery.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.pr8.delivery.domain.Delivery;
import ru.pr8.delivery.domain.DeliveryStatus;
import ru.pr8.delivery.domain.DeliveryStatusHistory;
import ru.pr8.delivery.repository.DeliveryRepository;
import ru.pr8.delivery.repository.DeliveryStatusHistoryRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {
    private final DeliveryRepository repository = mock(DeliveryRepository.class);
    private final DeliveryStatusHistoryRepository historyRepository = mock(DeliveryStatusHistoryRepository.class);
    private final DeliveryService service = new DeliveryService(
            repository, historyRepository, new DeliveryTimelinePublisher());

    @Test
    void allowsExpectedTransition() {
        Delivery delivery = delivery(DeliveryStatus.DELIVERY_CREATED);
        when(repository.findById(1L)).thenReturn(Mono.just(delivery));
        when(repository.save(any(Delivery.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(historyRepository.save(any(DeliveryStatusHistory.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(service.updateStatus(1L, DeliveryStatus.IN_TRANSIT))
                .expectNextMatches(response -> response.status().equals("IN_TRANSIT"))
                .verifyComplete();
    }

    @Test
    void rejectsSkippingTransition() {
        when(repository.findById(1L)).thenReturn(Mono.just(delivery(DeliveryStatus.DELIVERY_CREATED)));

        StepVerifier.create(service.updateStatus(1L, DeliveryStatus.DELIVERED))
                .expectError(InvalidDeliveryStateException.class)
                .verify();
    }

    private Delivery delivery(DeliveryStatus status) {
        Instant now = Instant.now();
        return new Delivery(
                1L, UUID.randomUUID(), 10L, "Anna", "Moscow", new BigDecimal("100.00"),
                "Courier", status, "trace", now, now);
    }
}
