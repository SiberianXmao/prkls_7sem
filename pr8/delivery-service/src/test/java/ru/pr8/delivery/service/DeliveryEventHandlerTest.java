package ru.pr8.delivery.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.pr8.delivery.domain.Delivery;
import ru.pr8.delivery.domain.DeliveryStatusHistory;
import ru.pr8.delivery.event.OrderCreatedEvent;
import ru.pr8.delivery.repository.DeliveryRepository;
import ru.pr8.delivery.repository.DeliveryStatusHistoryRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeliveryEventHandlerTest {
    private final DeliveryRepository deliveryRepository = mock(DeliveryRepository.class);
    private final DeliveryStatusHistoryRepository historyRepository = mock(DeliveryStatusHistoryRepository.class);
    private final DeliveryTimelinePublisher publisher = new DeliveryTimelinePublisher();
    private final DeliveryEventHandler handler = new DeliveryEventHandler(
            deliveryRepository, historyRepository, publisher, "NOT_ASSIGNED");

    @Test
    void createsOneDeliveryAndTimeline() {
        UUID eventId = UUID.randomUUID();
        var event = new OrderCreatedEvent(
                eventId, 10L, "Anna", "Moscow", new BigDecimal("100.00"), "trace", Instant.now());
        when(deliveryRepository.existsBySourceEventId(eventId)).thenReturn(Mono.just(false));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery source = invocation.getArgument(0);
            return Mono.just(new Delivery(
                    1L, source.sourceEventId(), source.orderId(), source.customerName(), source.deliveryAddress(),
                    source.orderTotal(), source.courier(), source.status(), source.requestId(),
                    source.createdAt(), source.updatedAt()));
        });
        when(historyRepository.save(any(DeliveryStatusHistory.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(handler.handle(event)).verifyComplete();

        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void ignoresRepeatedEvent() {
        UUID eventId = UUID.randomUUID();
        when(deliveryRepository.existsBySourceEventId(eventId)).thenReturn(Mono.just(true));
        var event = new OrderCreatedEvent(
                eventId, 10L, "Anna", "Moscow", new BigDecimal("100.00"), "trace", Instant.now());

        StepVerifier.create(handler.handle(event)).verifyComplete();

        verify(deliveryRepository, never()).save(any());
    }
}
