package ru.pr4.server;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import ru.pr4.client.ProductRSocketClient;
import ru.pr4.model.PriceUpdate;
import ru.pr4.model.SupplierProduct;
import ru.pr4.model.UpdateAcknowledgement;
import ru.pr4.protocol.JsonPayloadCodec;
import ru.pr4.repository.InMemoryProductRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TcpIntegrationTest {
    @Test
    void clientAndServerExchangeDataOverTcp() {
        JsonPayloadCodec codec = new JsonPayloadCodec();
        InMemoryProductRepository repository = InMemoryProductRepository.sample();

        try (ProductRSocketServer server = new ProductRSocketServer(repository, codec)) {
            server.start("127.0.0.1", 0);
            try (ProductRSocketClient client = ProductRSocketClient.connect(
                    "127.0.0.1", server.port(), codec)) {
                SupplierProduct product = client.findProduct(1).block(Duration.ofSeconds(3));
                List<SupplierProduct> streamed = client.streamProducts(3, Duration.ZERO)
                        .collectList().block(Duration.ofSeconds(3));
                client.sendViewEvent(1, "integration-test").block(Duration.ofSeconds(3));
                List<UpdateAcknowledgement> acknowledgements = client.updatePrices(Flux.just(
                                new PriceUpdate(1, new BigDecimal("7100.00"))))
                        .collectList().block(Duration.ofSeconds(3));

                assertEquals("Механическая клавиатура", product.product());
                assertEquals(3, streamed.size());
                assertEquals(1, repository.viewEvents().size());
                assertEquals(new BigDecimal("7100.00"),
                        acknowledgements.getFirst().newPrice());
            }
        }
    }
}
