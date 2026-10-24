package ru.pr8.order.client;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class SupplierCatalogGateway {
    private final SupplierClient supplierClient;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final RetryRegistry retryRegistry;

    public SupplierCatalogGateway(
            SupplierClient supplierClient,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            RetryRegistry retryRegistry) {
        this.supplierClient = supplierClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.retryRegistry = retryRegistry;
    }

    public ProductSnapshot requireAvailable(long productId, int quantity) {
        Retry retry = retryRegistry.retry("supplierCatalog");
        Supplier<ProductSnapshot> request = Retry.decorateSupplier(
                retry,
                () -> supplierClient.checkAvailability(productId, quantity));
        return circuitBreakerFactory.create("supplierCatalog").run(
                request,
                failure -> {
                    throw new SupplierUnavailableException(
                            "Supplier Service is unavailable or rejected the product request",
                            failure);
                });
    }
}
