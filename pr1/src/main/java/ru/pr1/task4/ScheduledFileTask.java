package ru.pr1.task4;

import java.util.Objects;

public record ScheduledFileTask(FileTask task, int productionDelayMillis) {
    public ScheduledFileTask {
        Objects.requireNonNull(task, "task");
        if (task.terminationSignal() || productionDelayMillis < 0) {
            throw new IllegalArgumentException("Некорректная производственная задача");
        }
    }
}
