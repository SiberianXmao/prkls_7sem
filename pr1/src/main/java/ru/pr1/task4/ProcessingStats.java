package ru.pr1.task4;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

final class ProcessingStats {
    private final LongAdder generated = new LongAdder();
    private final LongAdder processed = new LongAdder();
    private final Map<FileType, LongAdder> byType = new EnumMap<>(FileType.class);
    private final Set<Long> generatedIds = ConcurrentHashMap.newKeySet();
    private final Set<Long> processedIds = ConcurrentHashMap.newKeySet();

    ProcessingStats() {
        for (FileType type : FileType.values()) {
            byType.put(type, new LongAdder());
        }
    }

    void generated(FileTask task) {
        if (!generatedIds.add(task.id())) {
            throw new IllegalStateException("Producer создал повторный ID: " + task.id());
        }
        generated.increment();
    }

    void processed(FileTask task) {
        if (!processedIds.add(task.id())) {
            throw new IllegalStateException("Задача обработана повторно: " + task.id());
        }
        byType.get(task.type()).increment();
        processed.increment();
    }

    ProcessingStatsSnapshot snapshot(long elapsedMillis) {
        Map<FileType, Long> counts = new EnumMap<>(FileType.class);
        byType.forEach((type, value) -> counts.put(type, value.sum()));
        return new ProcessingStatsSnapshot(
                generated.sum(), processed.sum(), Map.copyOf(counts),
                Set.copyOf(generatedIds), Set.copyOf(processedIds), elapsedMillis);
    }
}
