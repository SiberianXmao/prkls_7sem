package ru.pr4.app;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr4.client.ProductRSocketClient;
import ru.pr4.model.PriceUpdate;
import ru.pr4.model.SupplierProduct;

import java.math.BigDecimal;
import java.time.Duration;

public final class DemoRunner {
    public void run(ProductRSocketClient client) {
        requestResponse(client);
        requestStream(client);
        fireAndForget(client);
        requestChannel(client);
    }

    private void requestResponse(ProductRSocketClient client) {
        System.out.println("\n=== Request-Response ===");
        SupplierProduct product = client.findProduct(3).block(Duration.ofSeconds(5));
        System.out.println("CLIENT received: " + product);

        client.findProduct(999)
                .doOnError(error -> System.out.println(
                        "CLIENT handled error: " + error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .block(Duration.ofSeconds(5));
    }

    private void requestStream(ProductRSocketClient client) {
        System.out.println("\n=== Request-Stream ===");
        client.streamProducts(4, Duration.ofMillis(120))
                .index()
                .doOnNext(indexed -> System.out.printf(
                        "CLIENT stream #%d: %s — %s%n",
                        indexed.getT1() + 1,
                        indexed.getT2().product(),
                        indexed.getT2().price().toPlainString()))
                .blockLast(Duration.ofSeconds(5));
    }

    private void fireAndForget(ProductRSocketClient client) {
        System.out.println("\n=== Fire-and-Forget ===");
        client.sendViewEvent(2, "console-client")
                .block(Duration.ofSeconds(5));
        System.out.println("CLIENT sent view event without response payload");
    }

    private void requestChannel(ProductRSocketClient client) {
        System.out.println("\n=== Channel ===");
        Flux<PriceUpdate> updates = Flux.just(
                        new PriceUpdate(1, new BigDecimal("7290.00")),
                        new PriceUpdate(2, new BigDecimal("3190.00")),
                        new PriceUpdate(4, new BigDecimal("11490.00")))
                .delayElements(Duration.ofMillis(100))
                .doOnNext(update -> System.out.printf(
                        "CLIENT -> SERVER: productId=%d, newPrice=%s%n",
                        update.productId(), update.newPrice().toPlainString()));

        client.updatePrices(updates)
                .doOnNext(ack -> System.out.printf(
                        "SERVER -> CLIENT: productId=%d, %s -> %s, status=%s%n",
                        ack.productId(), ack.oldPrice().toPlainString(),
                        ack.newPrice().toPlainString(), ack.status()))
                .blockLast(Duration.ofSeconds(5));
    }
}
