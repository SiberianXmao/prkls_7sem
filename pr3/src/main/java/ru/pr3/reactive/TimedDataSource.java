package ru.pr3.reactive;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import ru.pr3.model.SupplierProduct;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TimedDataSource {
    private TimedDataSource() {
    }

    public static Flowable<SupplierProduct> atInterval(
            List<SupplierProduct> products,
            Duration interval,
            Scheduler scheduler) {
        Objects.requireNonNull(products, "products");
        Objects.requireNonNull(interval, "interval");
        Objects.requireNonNull(scheduler, "scheduler");
        if (interval.isNegative() || interval.isZero()) {
            throw new IllegalArgumentException("Интервал должен быть положительным");
        }

        return Flowable.fromIterable(products)
                .concatMap(product -> Flowable.just(product)
                        .delay(interval.toMillis(), TimeUnit.MILLISECONDS, scheduler));
    }
}
