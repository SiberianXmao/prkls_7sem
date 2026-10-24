package ru.pr1.task3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Асинхронный конвейер Input -> Validation -> Calculation -> Transformation -> Result. */
public final class AsyncRequestProcessor implements AutoCloseable {
    private final ExecutorService executor;
    private final Duration timeout;

    public AsyncRequestProcessor(int workerCount, Duration timeout) {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Число worker-потоков должно быть положительным");
        }
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        if (timeout.isZero() || timeout.isNegative()) {
            throw new IllegalArgumentException("Timeout должен быть положительным");
        }
        this.executor = Executors.newFixedThreadPool(workerCount);
    }

    public CompletableFuture<RequestResult> process(Request input) {
        String requestId = input == null || input.id() == null ? "<unknown>" : input.id();

        return CompletableFuture.completedFuture(input)                  // Input
                .thenApply(this::validate)                               // Validation
                .thenCompose(this::calculateAsync)                       // Calculation
                .thenApply(total -> formatSuccess(requestId, total))     // Transformation
                .orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS)
                .handle((result, error) -> error == null
                        ? result
                        : formatError(requestId, unwrap(error)));        // Result
    }

    private Request validate(Request request) {
        if (request == null) {
            throw new IllegalArgumentException("Запрос отсутствует");
        }
        if (request.id() == null || request.id().isBlank()) {
            throw new IllegalArgumentException("ID запроса не задан");
        }
        if (request.quantity() <= 0) {
            throw new IllegalArgumentException("Количество должно быть больше нуля");
        }
        if (request.unitPrice() == null || request.unitPrice().signum() < 0) {
            throw new IllegalArgumentException("Цена не может быть отрицательной");
        }
        if (request.discountPercent() < 0 || request.discountPercent() > 100) {
            throw new IllegalArgumentException("Скидка должна находиться в диапазоне 0..100");
        }
        return request;
    }

    private CompletableFuture<BigDecimal> calculateAsync(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            BigDecimal subtotal = request.unitPrice()
                    .multiply(BigDecimal.valueOf(request.quantity()));
            BigDecimal multiplier = BigDecimal.valueOf(100L - request.discountPercent())
                    .movePointLeft(2);
            return subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        }, executor);
    }

    private RequestResult formatSuccess(String requestId, BigDecimal total) {
        return new RequestResult(requestId, true, "Итоговая стоимость: " + total.toPlainString());
    }

    private RequestResult formatError(String requestId, Throwable error) {
        return new RequestResult(requestId, false, "Ошибка: " + error.getMessage());
    }

    private Throwable unwrap(Throwable error) {
        Throwable current = error;
        while (current instanceof CompletionException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("ExecutorService не завершился");
                }
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
