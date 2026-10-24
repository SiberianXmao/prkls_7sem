package ru.pr1.task4;

import java.util.Objects;

public record FileTask(long id, FileType type, int size, boolean terminationSignal) {
    public FileTask {
        if (!terminationSignal) {
            Objects.requireNonNull(type, "Тип файла не задан");
            if (id < 0 || size < 10 || size > 100) {
                throw new IllegalArgumentException("ID должен быть неотрицательным, размер — от 10 до 100");
            }
        }
    }

    public static FileTask data(long id, FileType type, int size) {
        return new FileTask(id, type, size, false);
    }

    public static FileTask poisonPill() {
        return new FileTask(-1, null, 0, true);
    }
}
