package ru.pr1.task1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SplittableRandom;

/** Воспроизводимый CPU-bound эксперимент для задания 1. */
public final class ArrayExperiment {
    private static final int STUDENT_NUMBER = 27;
    private static final int VARIANT = ((STUDENT_NUMBER - 1) % 3) + 1;
    private static final long SEED = 27_2026L;
    private static volatile int blackHole;

    private ArrayExperiment() {
    }

    public static void main(String[] args) throws Exception {

        Locale.setDefault(Locale.US);

        int arraySize = Integer.getInteger("array.size", 50_000_000);
        int measuredRuns = Integer.getInteger("runs", 5);
        int warmupRuns = Integer.getInteger("warmup", 2);
        int processors = Runtime.getRuntime().availableProcessors();

        int workers = Math.max(1, processors);
        int[] thresholds = {25_000, 100_000, 500_000};

        int[] values = createArray(arraySize);
        int expected = ArrayMinCalculator.sequentialMin(values);

        System.out.printf("Размер массива: %,d, seed: %d%n", arraySize, SEED);
        System.out.printf("Доступно процессоров: %d, worker-потоков: %d%n", processors, workers);
        System.out.printf("Warm-up: %d, измеряемых запусков: %d%n", warmupRuns, measuredRuns);

        // прогрев jvm
        for (int run = 0; run < warmupRuns; run++) {
            consume(ArrayMinCalculator.sequentialMin(values), expected);
            consume(ArrayMinCalculator.executorMin(values, workers), expected);
            for (int threshold : thresholds) {
                consume(ArrayMinCalculator.forkJoinMin(values, threshold, workers), expected);
            }
        }

        Map<String, List<Long>> measurements = new LinkedHashMap<>();
        measurements.put("Последовательно", measure(measuredRuns,
                () -> ArrayMinCalculator.sequentialMin(values), expected));

        measurements.put("ExecutorService", measure(measuredRuns,
                () -> ArrayMinCalculator.executorMin(values, workers), expected));

        for (int threshold : thresholds) {
            measurements.put("ForkJoin, threshold=" + threshold, measure(measuredRuns,
                    () -> ArrayMinCalculator.forkJoinMin(values, threshold, workers), expected));
        }

        double sequentialMedian = medianMillis(measurements.get("Последовательно"));
        System.out.println("\nРеализация\tЗапуски, мс\tСреднее, мс\tМедиана, мс\tУскорение");
        measurements.forEach((name, times) -> {
            double mean = times.stream().mapToLong(Long::longValue).average().orElseThrow() / 1_000_000.0;
            double median = medianMillis(times);
            System.out.printf("%s\t%s\t%.3f\t%.3f\t%.2f%n",
                    name, formatMillis(times), mean, median, sequentialMedian / median);
        });
        System.out.println("Контрольный минимум: " + blackHole);
    }

    private static int[] createArray(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Размер массива должен быть положительным");
        }
        SplittableRandom random = new SplittableRandom(SEED);
        int[] values = new int[size];
        for (int index = 0; index < size; index++) {
            values[index] = random.nextInt();
        }
        return values;
    }

    private static List<Long> measure(int runs, CheckedIntSupplier action, int expected) throws Exception {
        if (runs < 5) {
            throw new IllegalArgumentException("По методичке нужно не менее пяти измерений");
        }
        List<Long> times = new ArrayList<>(runs);
        for (int run = 0; run < runs; run++) {
            long started = System.nanoTime();
            int result = action.getAsInt();
            times.add(System.nanoTime() - started);
            consume(result, expected);
        }
        return List.copyOf(times);
    }

    private static void consume(int actual, int expected) {
        if (actual != expected) {
            throw new IllegalStateException("Результаты не совпали: " + actual + " != " + expected);
        }
        blackHole = actual;
    }

    private static double medianMillis(List<Long> nanos) {
        long[] sorted = nanos.stream().mapToLong(Long::longValue).sorted().toArray();
        int middle = sorted.length / 2;
        double median = sorted.length % 2 == 0
                ? (sorted[middle - 1] + sorted[middle]) / 2.0
                : sorted[middle];
        return median / 1_000_000.0;
    }

    private static String formatMillis(List<Long> nanos) {
        return Arrays.toString(nanos.stream()
                .mapToDouble(value -> value / 1_000_000.0)
                .mapToObj(value -> String.format("%.3f", value))
                .toArray(String[]::new));
    }

    @FunctionalInterface
    private interface CheckedIntSupplier {
        int getAsInt() throws Exception;
    }
}
