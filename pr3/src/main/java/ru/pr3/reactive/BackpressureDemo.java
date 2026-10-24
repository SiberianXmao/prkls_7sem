package ru.pr3.reactive;

import io.reactivex.rxjava3.core.BackpressureOverflowStrategy;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.pr3.model.BackpressureReport;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public final class BackpressureDemo {
    public BackpressureReport run(int itemCount, int bufferCapacity, Duration consumerDelay) {
        if (itemCount <= 0 || bufferCapacity <= 0 || consumerDelay.isNegative()) {
            throw new IllegalArgumentException("Некорректные параметры backpressure-демонстрации");
        }

        AtomicInteger produced = new AtomicInteger();
        AtomicInteger consumed = new AtomicInteger();
        AtomicInteger dropped = new AtomicInteger();

        Flowable.<Integer>create(emitter -> {
                    for (int value = 1; value <= itemCount && !emitter.isCancelled(); value++) {
                        produced.incrementAndGet();
                        emitter.onNext(value);
                    }
                    emitter.onComplete();
                }, BackpressureStrategy.MISSING)
                .subscribeOn(Schedulers.io())
                .onBackpressureBuffer(
                        bufferCapacity,
                        dropped::incrementAndGet,
                        BackpressureOverflowStrategy.DROP_OLDEST)
                .observeOn(Schedulers.computation(), false, 1)
                .blockingForEach(value -> {
                    sleep(consumerDelay);
                    consumed.incrementAndGet();
                });

        return new BackpressureReport(
                produced.get(), consumed.get(), dropped.get(), bufferCapacity);
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Медленный consumer был прерван", exception);
        }
    }
}
