package ru.pr4.server;

import io.rsocket.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.pr4.model.PriceUpdate;
import ru.pr4.model.ProductRequest;
import ru.pr4.model.ProductStreamRequest;
import ru.pr4.model.SupplierProduct;
import ru.pr4.model.UpdateAcknowledgement;
import ru.pr4.model.ViewEvent;
import ru.pr4.protocol.JsonPayloadCodec;
import ru.pr4.repository.InMemoryProductRepository;
import ru.pr4.repository.ProductNotFoundException;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductRSocketHandlerTest {
    private InMemoryProductRepository repository;
    private JsonPayloadCodec codec;
    private ProductRSocketHandler handler;

    @BeforeEach
    void setUp() {
        repository = InMemoryProductRepository.sample();
        codec = new JsonPayloadCodec();
        handler = new ProductRSocketHandler(repository, codec);
    }

    @Test
    void requestResponseReturnsOneProduct() {
        StepVerifier.create(handler
                        .requestResponse(codec.encode(new ProductRequest(3)))
                        .map(payload -> codec.decodeAndRelease(payload, SupplierProduct.class)))
                .assertNext(product -> assertEquals("Монитор 27 дюймов", product.product()))
                .verifyComplete();
    }

    @Test
    void requestResponseReturnsControlledErrorForUnknownProduct() {
        StepVerifier.create(handler.requestResponse(codec.encode(new ProductRequest(999))))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void requestStreamEmitsProductsGradually() {
        StepVerifier.withVirtualTime(() -> handler
                        .requestStream(codec.encode(new ProductStreamRequest(2, 100)))
                        .map(payload -> codec.decodeAndRelease(payload, SupplierProduct.class)))
                .expectSubscription()
                .expectNoEvent(Duration.ofMillis(99))
                .thenAwait(Duration.ofMillis(1))
                .assertNext(product -> assertEquals(1, product.productId()))
                .thenAwait(Duration.ofMillis(100))
                .assertNext(product -> assertEquals(2, product.productId()))
                .verifyComplete();
    }

    @Test
    void fireAndForgetRecordsEvent() {
        ViewEvent event = new ViewEvent(2, "test-client");

        StepVerifier.create(handler.fireAndForget(codec.encode(event))).verifyComplete();

        assertEquals(1, repository.viewEvents().size());
        assertEquals(event, repository.viewEvents().getFirst());
    }

    @Test
    void channelUpdatesPricesAndReturnsAcknowledgements() {
        Flux<Payload> requests = Flux.just(
                codec.encode(new PriceUpdate(1, new BigDecimal("7000.00"))),
                codec.encode(new PriceUpdate(2, new BigDecimal("3000.00"))));

        StepVerifier.create(handler.requestChannel(requests)
                        .map(payload -> codec.decodeAndRelease(
                                payload, UpdateAcknowledgement.class)))
                .assertNext(ack -> {
                    assertEquals(1, ack.productId());
                    assertEquals(new BigDecimal("7490.00"), ack.oldPrice());
                    assertEquals(new BigDecimal("7000.00"), ack.newPrice());
                })
                .assertNext(ack -> {
                    assertEquals(2, ack.productId());
                    assertEquals(new BigDecimal("3290.50"), ack.oldPrice());
                    assertEquals(new BigDecimal("3000.00"), ack.newPrice());
                })
                .verifyComplete();
    }
}
