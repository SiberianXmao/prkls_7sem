package ru.pr7.delivery.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.pr7.delivery.api.DeliveryResponse;
import ru.pr7.delivery.domain.DeliveryStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryEventPublisherTest {
    private static final Instant NOW = Instant.parse("2026-10-21T12:00:00Z");

    @Test
    void slowSubscriberReceivesLatestStatus() {
        DeliveryEventPublisher publisher = new DeliveryEventPublisher(
                Clock.fixed(NOW, ZoneOffset.UTC));

        StepVerifier.create(publisher.stream(7L), 0)
                .then(() -> {
                    publisher.publish(response(DeliveryStatus.CREATED), "CREATED");
                    publisher.publish(response(DeliveryStatus.ACCEPTED), "UPDATED");
                    publisher.publish(response(DeliveryStatus.IN_TRANSIT), "UPDATED");
                })
                .thenRequest(1)
                .assertNext(event -> assertThat(event.status())
                        .isEqualTo(DeliveryStatus.IN_TRANSIT))
                .thenCancel()
                .verify();
    }

    private DeliveryResponse response(DeliveryStatus status) {
        return new DeliveryResponse(
                7L,
                1L,
                "Клиент",
                "Москва",
                new BigDecimal("2490.00"),
                "Курьер",
                status,
                NOW,
                NOW);
    }
}
