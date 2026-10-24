package ru.pr4.app;

import ru.pr4.client.ProductRSocketClient;
import ru.pr4.protocol.JsonPayloadCodec;

public final class ClientMain {
    private ClientMain() {
    }

    public static void main(String[] args) {
        String host = args.length >= 1 ? args[0] : "127.0.0.1";
        int port = args.length >= 2 ? Integer.parseInt(args[1]) : 7000;

        try (ProductRSocketClient client = ProductRSocketClient.connect(
                host, port, new JsonPayloadCodec())) {
            new DemoRunner().run(client);
        }
        System.out.println("Client stopped.");
    }
}
