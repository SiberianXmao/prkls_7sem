package ru.pr7.delivery.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr7.delivery.domain.DeliveryStatus;
import ru.pr7.delivery.service.DeliveryNotFoundException;
import ru.pr7.delivery.service.DeliveryService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeliveryControllerTest {
    private DeliveryService service;
    private WebTestClient client;

    @BeforeEach
    void setUp() {
        service = mock(DeliveryService.class);
        client = WebTestClient.bindToController(new DeliveryController(service))
                .controllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsFluxFromCollectionEndpoint() {
        when(service.findAll(null, null)).thenReturn(Flux.just(response(1L), response(2L)));

        client.get()
                .uri("/api/deliveries")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[1].id").isEqualTo(2);
    }

    @Test
    void mapsMissingDeliveryTo404() {
        when(service.findById(99L)).thenReturn(Mono.error(new DeliveryNotFoundException(99L)));

        client.get()
                .uri("/api/deliveries/99")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.path").isEqualTo("/api/deliveries/99");
    }

    @Test
    void createsDeliveryWith201Status() {
        when(service.create(any(CreateDeliveryRequest.class))).thenReturn(Mono.just(response(5L)));

        client.post()
                .uri("/api/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"orderId":1,"courier":"Иван Курьер"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().location("/api/deliveries/5")
                .expectBody()
                .jsonPath("$.id").isEqualTo(5);
    }

    private DeliveryResponse response(long id) {
        Instant now = Instant.parse("2026-10-21T12:00:00Z");
        return new DeliveryResponse(
                id,
                1L,
                "Анна",
                "Москва",
                new BigDecimal("2490.00"),
                "Иван Курьер",
                DeliveryStatus.CREATED,
                now,
                now);
    }
}
