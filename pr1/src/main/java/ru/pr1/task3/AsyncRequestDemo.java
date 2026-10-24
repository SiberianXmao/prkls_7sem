package ru.pr1.task3;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class AsyncRequestDemo {
    private AsyncRequestDemo() {
    }

    public static void main(String[] args) {
        List<Request> requests = List.of(
                new Request("REQ-001", 3, new BigDecimal("149.90"), 10),
                new Request("REQ-002", 2, new BigDecimal("500.00"), 5),
                new Request("REQ-003", 0, new BigDecimal("10.00"), 0),
                new Request("REQ-004", 8, new BigDecimal("75.50"), 15)
        );

        try (AsyncRequestProcessor processor = new AsyncRequestProcessor(4, Duration.ofSeconds(2))) {
            List<CompletableFuture<RequestResult>> pipelines = requests.stream()
                    .map(processor::process)
                    .toList();

            // Все цепочки созданы до ожидания: запросы обрабатываются одновременно.
            pipelines.forEach(pipeline -> pipeline.thenAccept(System.out::println));
            CompletableFuture.allOf(pipelines.toArray(CompletableFuture[]::new)).join();
        }
    }
}
