package ru.pr4.server;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr4.model.PriceUpdate;
import ru.pr4.model.ProductRequest;
import ru.pr4.model.ProductStreamRequest;
import ru.pr4.model.ViewEvent;
import ru.pr4.protocol.JsonPayloadCodec;
import ru.pr4.repository.InMemoryProductRepository;
import ru.pr4.repository.ProductNotFoundException;

import java.time.Duration;

public final class ProductRSocketHandler implements RSocket {
    private final InMemoryProductRepository repository;
    private final JsonPayloadCodec codec;

    public ProductRSocketHandler(
            InMemoryProductRepository repository,
            JsonPayloadCodec codec) {
        this.repository = repository;
        this.codec = codec;
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        return Mono.fromCallable(() -> codec.decodeAndRelease(payload, ProductRequest.class))
                .flatMap(request -> repository.findById(request.productId())
                        .map(product -> Mono.just(codec.encode(product)))
                        .orElseGet(() -> Mono.error(
                                new ProductNotFoundException(request.productId()))));
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        ProductStreamRequest request;
        try {
            request = codec.decodeAndRelease(payload, ProductStreamRequest.class);
        } catch (RuntimeException exception) {
            return Flux.error(exception);
        }

        return Flux.fromIterable(repository.findAll())
                .take(request.limit())
                .delayElements(Duration.ofMillis(request.delayMillis()))
                .map(codec::encode);
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        return Mono.fromRunnable(() -> {
            ViewEvent event = codec.decodeAndRelease(payload, ViewEvent.class);
            repository.recordView(event);
            System.out.printf("SERVER FNF | client=%s viewed productId=%d%n",
                    event.clientId(), event.productId());
        });
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux.from(payloads)
                .concatMap(payload -> Mono
                        .fromCallable(() -> codec.decodeAndRelease(payload, PriceUpdate.class))
                        .map(repository::updatePrice)
                        .map(codec::encode));
    }
}
