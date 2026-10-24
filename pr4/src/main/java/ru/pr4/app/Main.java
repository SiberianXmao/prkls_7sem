package ru.pr4.app;

import ru.pr4.client.ProductRSocketClient;
import ru.pr4.protocol.JsonPayloadCodec;
import ru.pr4.repository.InMemoryProductRepository;
import ru.pr4.server.ProductRSocketServer;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        JsonPayloadCodec codec = new JsonPayloadCodec();
        InMemoryProductRepository repository = InMemoryProductRepository.sample();

        try (ProductRSocketServer server = new ProductRSocketServer(repository, codec)) {
            server.start("127.0.0.1", 0);
            try (ProductRSocketClient client = ProductRSocketClient.connect(
                    "127.0.0.1", server.port(), codec)) {
                new DemoRunner().run(client);
            }
        }
        System.out.println("\nDemo finished; client and server stopped correctly.");
    }
}
