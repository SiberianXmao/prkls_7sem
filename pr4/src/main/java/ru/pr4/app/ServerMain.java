package ru.pr4.app;

import reactor.core.publisher.Mono;
import ru.pr4.protocol.JsonPayloadCodec;
import ru.pr4.repository.InMemoryProductRepository;
import ru.pr4.server.ProductRSocketServer;

import java.time.Duration;

public final class ServerMain {
    private ServerMain() {
    }

    public static void main(String[] args) {
        int port = args.length >= 1 ? Integer.parseInt(args[0]) : 7000;
        long lifetimeSeconds = args.length >= 2 ? Long.parseLong(args[1]) : 60;

        try (ProductRSocketServer server = new ProductRSocketServer(
                InMemoryProductRepository.sample(), new JsonPayloadCodec())) {
            server.start("0.0.0.0", port);
            System.out.printf("Server will work for %d seconds.%n", lifetimeSeconds);
            Mono.delay(Duration.ofSeconds(lifetimeSeconds)).block();
        }
        System.out.println("Server stopped.");
    }
}
