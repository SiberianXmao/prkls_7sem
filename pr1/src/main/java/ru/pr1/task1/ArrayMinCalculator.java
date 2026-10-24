package ru.pr1.task1;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/** Поиск минимума */
public final class ArrayMinCalculator {
    private ArrayMinCalculator() {
    }

    // для последовательного вычисления
    public static int sequentialMin(int[] values) {
        requireNotEmpty(values);
        return minInRange(values, 0, values.length);
    }

    // для нескольких потоков
    public static int executorMin(int[] values, int requestedWorkers) throws InterruptedException {
        requireNotEmpty(values);
        if (requestedWorkers <= 0) {
            throw new IllegalArgumentException("Число потоков должно быть положительным");
        }

        int workers = Math.min(requestedWorkers, values.length);
        ExecutorService executor = Executors.newFixedThreadPool(workers);
        List<Future<Integer>> futures = new ArrayList<>(workers);

        try {
            for (int worker = 0; worker < workers; worker++) {
                int from = worker * values.length / workers;
                int to = (worker + 1) * values.length / workers;
                futures.add(executor.submit(() -> minInRange(values, from, to)));
            }
            executor.shutdown();

            int result = Integer.MAX_VALUE;
            for (Future<Integer> future : futures) {
                result = Math.min(result, future.get());
            }
            return result;
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Ошибка worker-потока", exception.getCause());
        } catch (InterruptedException exception) {
            futures.forEach(future -> future.cancel(true));
            executor.shutdownNow();
            throw exception;
        } finally {
            shutdownExecutor(executor);
        }
    }

    // через пулл потоков
    public static int forkJoinMin(int[] values, int threshold, int parallelism) {
        requireNotEmpty(values);
        if (threshold <= 0) {
            throw new IllegalArgumentException("Порог должен быть положительным");
        }
        if (parallelism <= 0) {
            throw new IllegalArgumentException("Параллелизм должен быть положительным");
        }

        ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            return pool.invoke(new MinTask(values, 0, values.length, threshold));
        } finally {
            pool.shutdown();
        }
    }

    private static int minInRange(int[] values, int from, int to) {
        int result = Integer.MAX_VALUE;
        for (int index = from; index < to; index++) {
            result = Math.min(result, values[index]);
        }
        return result;
    }

    private static void requireNotEmpty(int[] values) {
        Objects.requireNonNull(values, "Массив не должен быть null");
        if (values.length == 0) {
            throw new IllegalArgumentException("Массив не должен быть пустым");
        }
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

    private static final class MinTask extends RecursiveTask<Integer> {
        private final int[] values;
        private final int from;
        private final int to;
        private final int threshold;

        private MinTask(int[] values, int from, int to, int threshold) {
            this.values = values;
            this.from = from;
            this.to = to;
            this.threshold = threshold;
        }

        @Override
        protected Integer compute() {
            if (to - from <= threshold) {
                return minInRange(values, from, to);
            }

            int middle = from + (to - from) / 2;
            MinTask left = new MinTask(values, from, middle, threshold);
            MinTask right = new MinTask(values, middle, to, threshold);
            left.fork();
            int rightResult = right.compute();
            return Math.min(left.join(), rightResult);
        }
    }
}
