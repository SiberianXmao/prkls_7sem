package ru.pr1.task4;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Конечная Producer/Consumer-модель с общей маршрутизацией по типу файла. */
public final class FileProcessingSimulation {
    private static final long SEED = 27_2026L;

    private FileProcessingSimulation() {
    }

    public static void main(String[] args) throws Exception {
        List<ScheduledFileTask> tasks = createTasks(30, SEED, 1, 4);
        ProcessingStatsSnapshot stats = run(tasks, 5, 3, 2);

        System.out.println("Generated: " + stats.generated());
        System.out.println("Processed: " + stats.processed());
        System.out.println("XML: " + stats.count(FileType.XML));
        System.out.println("JSON: " + stats.count(FileType.JSON));
        System.out.println("XLS: " + stats.count(FileType.XLS));
        System.out.println("Total processing time: " + stats.elapsedMillis() + " ms");
    }

    public static List<ScheduledFileTask> createTasks(
            int count, long seed, int minProductionDelayMillis, int maxProductionDelayMillis) {
        if (count <= 0 || minProductionDelayMillis < 0
                || maxProductionDelayMillis < minProductionDelayMillis) {
            throw new IllegalArgumentException("Некорректные параметры генерации");
        }
        SplittableRandom random = new SplittableRandom(seed);
        FileType[] types = FileType.values();
        List<ScheduledFileTask> tasks = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            tasks.add(new ScheduledFileTask(
                    FileTask.data(index, types[index % types.length], random.nextInt(10, 101)),
                    random.nextInt(minProductionDelayMillis, maxProductionDelayMillis + 1)));
        }
        return List.copyOf(tasks);
    }

    public static ProcessingStatsSnapshot run(
            List<ScheduledFileTask> tasks,
            int queueCapacity,
            int consumerCount,
            int processingMillisPerSizeUnit) throws InterruptedException {
        if (tasks.isEmpty() || queueCapacity <= 0 || consumerCount <= 0
                || processingMillisPerSizeUnit < 0) {
            throw new IllegalArgumentException("Некорректные параметры симуляции");
        }

        BlockingQueue<FileTask> queue = new ArrayBlockingQueue<>(queueCapacity);
        ProcessingStats stats = new ProcessingStats();
        ExecutorService executor = Executors.newFixedThreadPool(consumerCount + 1);
        long started = System.nanoTime();

        Future<?> producer = executor.submit(
                () -> produce(tasks, queue, consumerCount, stats));
        List<Future<?>> consumers = new ArrayList<>(consumerCount);
        for (int index = 0; index < consumerCount; index++) {
            consumers.add(executor.submit(() -> consume(queue, stats, processingMillisPerSizeUnit)));
        }
        executor.shutdown();

        try {
            producer.get();
            for (Future<?> consumer : consumers) {
                consumer.get();
            }
        } catch (ExecutionException exception) {
            executor.shutdownNow();
            throw new IllegalStateException("Ошибка producer/consumer", exception.getCause());
        } catch (InterruptedException exception) {
            producer.cancel(true);
            consumers.forEach(consumer -> consumer.cancel(true));
            executor.shutdownNow();
            throw exception;
        } finally {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        }

        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        ProcessingStatsSnapshot snapshot = stats.snapshot(elapsedMillis);
        snapshot.verifyInvariants();
        return snapshot;
    }

    private static void produce(
            List<ScheduledFileTask> tasks,
            BlockingQueue<FileTask> queue,
            int consumerCount,
            ProcessingStats stats) {
        boolean interrupted = false;
        try {
            for (ScheduledFileTask scheduled : tasks) {
                Thread.sleep(scheduled.productionDelayMillis());
                queue.put(scheduled.task()); // При заполнении очереди здесь возникает backpressure
                stats.generated(scheduled.task());
            }
        } catch (InterruptedException exception) {
            interrupted = true;
        } finally {
            // Каждый consumer получает ровно один конечный сигнал, даже если producer прервали
            int signalsSent = 0;
            while (signalsSent < consumerCount) {
                try {
                    queue.put(FileTask.poisonPill());
                    signalsSent++;
                } catch (InterruptedException exception) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void consume(
            BlockingQueue<FileTask> queue,
            ProcessingStats stats,
            int processingMillisPerSizeUnit) {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                FileTask task = queue.take();
                if (task.terminationSignal()) {
                    return;
                }
                Thread.sleep((long) task.size() * processingMillisPerSizeUnit);
                stats.processed(task);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
