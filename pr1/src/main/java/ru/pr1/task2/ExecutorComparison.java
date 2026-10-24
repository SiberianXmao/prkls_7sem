package ru.pr1.task2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public final class ExecutorComparison {
    private static final long SEED = 27_2026L;

    private ExecutorComparison() {
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        int taskCount = Integer.getInteger("tasks", 750);
        int fixedPoolSize = Integer.getInteger("pool.size", 50);
        int runs = Integer.getInteger("runs", 3);
        List<BlockingTaskSpec> specs = createSpecs(taskCount, 20, 80, SEED);

        // Небольшой прогрев выполняется на одном и том же наборе параметров
        List<BlockingTaskSpec> warmup = specs.subList(0, Math.min(50, specs.size()));
        runFixed(warmup, Math.min(10, fixedPoolSize));
        runVirtual(warmup);

        System.out.printf("Задач: %d, fixed pool: %d, задержка: 20..80 мс, seed: %d%n",
                taskCount, fixedPoolSize, SEED);
        System.out.println("Executor\tВремена запусков, мс\tСреднее, мс");
        printMeasurements("Fixed thread pool", runs, () -> runFixed(specs, fixedPoolSize));
        printMeasurements("Virtual thread per task", runs, () -> runVirtual(specs));
    }

    public static List<BlockingTaskSpec> createSpecs(
            int taskCount, int minDelayMillis, int maxDelayMillis, long seed) {
        if (taskCount <= 0 || minDelayMillis < 0 || maxDelayMillis < minDelayMillis) {
            throw new IllegalArgumentException("Некорректные параметры набора задач");
        }
        SplittableRandom random = new SplittableRandom(seed);
        List<BlockingTaskSpec> specs = new ArrayList<>(taskCount);
        for (int id = 0; id < taskCount; id++) {
            specs.add(new BlockingTaskSpec(
                    id,
                    random.nextInt(minDelayMillis, maxDelayMillis + 1),
                    random.nextInt()));
        }
        return List.copyOf(specs);
    }

    public static List<BlockingTaskResult> runFixed(List<BlockingTaskSpec> specs, int poolSize)
            throws InterruptedException {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Размер пула должен быть положительным");
        }
        return execute(specs, Executors.newFixedThreadPool(poolSize));
    }

    public static List<BlockingTaskResult> runVirtual(List<BlockingTaskSpec> specs)
            throws InterruptedException {
        return execute(specs, Executors.newVirtualThreadPerTaskExecutor());
    }

    private static List<BlockingTaskResult> execute(
            List<BlockingTaskSpec> specs, ExecutorService executor) throws InterruptedException {
        List<Future<BlockingTaskResult>> futures = new ArrayList<>(specs.size());
        try {
            for (BlockingTaskSpec spec : specs) {
                futures.add(executor.submit(() -> perform(spec)));
            }
            executor.shutdown();

            List<BlockingTaskResult> results = new ArrayList<>(specs.size());
            for (Future<BlockingTaskResult> future : futures) {
                results.add(future.get());
            }
            verifyResults(specs, results);
            return List.copyOf(results);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Blocking-задача завершилась с ошибкой", exception.getCause());
        } catch (InterruptedException exception) {
            futures.forEach(future -> future.cancel(true));
            executor.shutdownNow();
            throw exception;
        } finally {
            shutdownExecutor(executor);
        }
    }

    private static BlockingTaskResult perform(BlockingTaskSpec spec) throws InterruptedException {
        long checksum = spec.input();
        for (int iteration = 0; iteration < 1_000; iteration++) {
            checksum = checksum * 31 + iteration;
        }
        // Имитация ожидания внешнего ресурса: это намеренно blocking-операция.
        Thread.sleep(spec.delayMillis());
        return new BlockingTaskResult(spec.id(), checksum);
    }

    private static void verifyResults(
            List<BlockingTaskSpec> specs, List<BlockingTaskResult> results) {
        if (results.size() != specs.size()) {
            throw new IllegalStateException("Получены не все результаты");
        }
        Set<Integer> ids = new HashSet<>();
        results.forEach(result -> ids.add(result.id()));
        if (ids.size() != specs.size()) {
            throw new IllegalStateException("ID результатов потеряны или продублированы");
        }
    }

    private static void printMeasurements(String name, int runs, CheckedAction action) throws Exception {
        List<Double> millis = new ArrayList<>(runs);
        for (int run = 0; run < runs; run++) {
            long started = System.nanoTime();
            action.run();
            millis.add((System.nanoTime() - started) / 1_000_000.0);
        }
        double mean = millis.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
        System.out.printf("%s\t%s\t%.3f%n", name, millis, mean);
    }

    private static void shutdownExecutor(ExecutorService executor) throws InterruptedException {
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            executor.shutdownNow();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("ExecutorService не завершился");
            }
        }
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
