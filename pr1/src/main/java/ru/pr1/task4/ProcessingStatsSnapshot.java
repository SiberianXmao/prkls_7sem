package ru.pr1.task4;

import java.util.Map;
import java.util.Set;

public record ProcessingStatsSnapshot(
        long generated,
        long processed,
        Map<FileType, Long> byType,
        Set<Long> generatedIds,
        Set<Long> processedIds,
        long elapsedMillis) {

    public void verifyInvariants() {
        long byTypeTotal = byType.values().stream().mapToLong(Long::longValue).sum();
        if (generated != processed) {
            throw new IllegalStateException("Generated != Processed");
        }
        if (processed != byTypeTotal) {
            throw new IllegalStateException("Processed != XML + JSON + XLS");
        }
        if (!generatedIds.equals(processedIds)) {
            throw new IllegalStateException("Множества созданных и обработанных ID не совпадают");
        }
    }

    public long count(FileType type) {
        return byType.getOrDefault(type, 0L);
    }
}
