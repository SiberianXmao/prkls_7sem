package ru.pr3.reactive;

import io.reactivex.rxjava3.schedulers.TestScheduler;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.Test;
import ru.pr3.model.SupplierProduct;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TimedDataSourceTest {
    @Test
    void emitsNewItemsAtSpecifiedIntervals() {
        TestScheduler scheduler = new TestScheduler();
        List<SupplierProduct> products = List.of(product("Первый"), product("Второй"));
        TestSubscriber<SupplierProduct> subscriber = TimedDataSource
                .atInterval(products, Duration.ofSeconds(1), scheduler)
                .test();

        subscriber.assertNoValues().assertNotComplete();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        subscriber.assertValueCount(1).assertNotComplete();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        subscriber.assertValueSequence(products).assertComplete().assertNoErrors();
    }

    @Test
    void rejectsZeroInterval() {
        assertThrows(IllegalArgumentException.class, () -> TimedDataSource.atInterval(
                List.of(product("Товар")), Duration.ZERO, new TestScheduler()));
    }

    private SupplierProduct product(String name) {
        return new SupplierProduct(1, "Поставщик", name, BigDecimal.TEN, 1);
    }
}
