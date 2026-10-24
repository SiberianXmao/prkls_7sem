package ru.pr4.client;

import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr4.model.PriceUpdate;
import ru.pr4.model.ProductRequest;
import ru.pr4.model.ProductStreamRequest;
import ru.pr4.model.SupplierProduct;
import ru.pr4.model.UpdateAcknowledgement;
import ru.pr4.model.ViewEvent;
import ru.pr4.protocol.JsonPayloadCodec;

import java.time.Duration;

public final class ProductRSocketClient implements AutoCloseable {
    private final RSocket socket;
    private final JsonPayloadCodec codec;

    private ProductRSocketClient(RSocket socket, JsonPayloadCodec codec) {
        this.socket = socket;
        this.codec = codec;
    }

    public static ProductRSocketClient connect(
            String host, int port, JsonPayloadCodec codec) {
        RSocket socket = RSocketConnector.create()
                .dataMimeType("application/json")
                .connect(TcpClientTransport.create(host, port))
                .block(Duration.ofSeconds(10));
        if (socket == null) {
            throw new IllegalStateException("Не удалось подключиться к RSocket-серверу");
        }
        return new ProductRSocketClient(socket, codec);
    }

    public Mono<SupplierProduct> findProduct(long productId) {
        return socket.requestResponse(codec.encode(new ProductRequest(productId)))
                .map(payload -> codec.decodeAndRelease(payload, SupplierProduct.class));
    }

    public Flux<SupplierProduct> streamProducts(int limit, Duration delay) {
        return socket.requestStream(codec.encode(
                        new ProductStreamRequest(limit, delay.toMillis())))
                .map(payload -> codec.decodeAndRelease(payload, SupplierProduct.class));
    }

    public Mono<Void> sendViewEvent(long productId, String clientId) {
        return socket.fireAndForget(codec.encode(new ViewEvent(productId, clientId)));
    }

    public Flux<UpdateAcknowledgement> updatePrices(Flux<PriceUpdate> updates) {
        return socket.requestChannel(updates.map(codec::encode))
                .map(payload -> codec.decodeAndRelease(payload, UpdateAcknowledgement.class));
    }

    @Override
    public void close() {
        socket.dispose();
    }
}
