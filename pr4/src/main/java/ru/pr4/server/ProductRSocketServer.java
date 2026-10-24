package ru.pr4.server;

import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.CloseableChannel;
import io.rsocket.transport.netty.server.TcpServerTransport;
import reactor.core.publisher.Mono;
import ru.pr4.protocol.JsonPayloadCodec;
import ru.pr4.repository.InMemoryProductRepository;

import java.net.InetSocketAddress;
import java.time.Duration;

public final class ProductRSocketServer implements AutoCloseable {
    private final InMemoryProductRepository repository;
    private final JsonPayloadCodec codec;
    private CloseableChannel channel;

    public ProductRSocketServer(
            InMemoryProductRepository repository,
            JsonPayloadCodec codec) {
        this.repository = repository;
        this.codec = codec;
    }

    public synchronized void start(String host, int port) {
        if (channel != null) {
            throw new IllegalStateException("Сервер уже запущен");
        }
        ProductRSocketHandler handler = new ProductRSocketHandler(repository, codec);
        channel = RSocketServer.create((setupPayload, sendingSocket) -> Mono.just(handler))
                .bind(TcpServerTransport.create(host, port))
                .block(Duration.ofSeconds(10));
        if (channel == null) {
            throw new IllegalStateException("Не удалось запустить RSocket-сервер");
        }
        System.out.printf("RSocket server started on %s:%d%n", host, port());
    }

    public synchronized int port() {
        if (channel == null) {
            throw new IllegalStateException("Сервер не запущен");
        }
        return ((InetSocketAddress) channel.address()).getPort();
    }

    public Mono<Void> onClose() {
        return channel == null ? Mono.empty() : channel.onClose();
    }

    @Override
    public synchronized void close() {
        if (channel != null) {
            channel.dispose();
            channel = null;
        }
    }
}
