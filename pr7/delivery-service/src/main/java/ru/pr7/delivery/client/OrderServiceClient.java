package ru.pr7.delivery.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class OrderServiceClient {
    private final WebClient webClient;
    private final Duration timeout;

    public OrderServiceClient(
            WebClient orderWebClient,
            @Value("${clients.order-service.timeout:3s}") Duration timeout) {
        this.webClient = orderWebClient;
        this.timeout = timeout;
    }

    public Mono<OrderSnapshot> findById(long orderId) {
        return webClient.get()
                .uri("/api/orders/{orderId}", orderId)
                .exchangeToMono(response -> readResponse(response, orderId))
                .timeout(timeout)
                .onErrorResume(
                        TimeoutException.class,
                        exception -> Mono.error(new RemoteOrderServiceException(
                                "Order Service не ответил за " + timeout, exception)))
                .onErrorResume(
                        WebClientRequestException.class,
                        exception -> Mono.error(new RemoteOrderServiceException(
                                "Order Service недоступен", exception)));
    }

    private Mono<OrderSnapshot> readResponse(ClientResponse response, long orderId) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(OrderSnapshot.class)
                    .switchIfEmpty(Mono.error(new RemoteOrderServiceException(
                            "Order Service вернул пустой ответ")));
        }

        if (response.statusCode().value() == 404) {
            return response.releaseBody()
                    .then(Mono.error(new RelatedOrderNotFoundException(orderId)));
        }

        HttpStatusCode status = response.statusCode();
        return response.releaseBody()
                .then(Mono.error(new RemoteOrderServiceException(
                        "Order Service вернул HTTP " + status.value())));
    }
}
