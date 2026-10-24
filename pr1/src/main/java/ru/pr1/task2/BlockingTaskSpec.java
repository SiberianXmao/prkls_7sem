package ru.pr1.task2;

public record BlockingTaskSpec(int id, int delayMillis, int input) {
    public BlockingTaskSpec {
        if (id < 0 || delayMillis < 0) {
            throw new IllegalArgumentException("ID и задержка не могут быть отрицательными");
        }
    }
}
