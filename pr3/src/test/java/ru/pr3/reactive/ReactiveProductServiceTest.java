package ru.pr3.reactive;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.TestSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.pr3.model.ProcessingIssue;
import ru.pr3.model.ProductView;
import ru.pr3.model.SupplierProduct;
import ru.pr3.model.SupplierTotal;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactiveProductServiceTest {
    private ReactiveProductService service;

    @BeforeEach
    void setUp() {
        service = new ReactiveProductService(Schedulers.trampoline(), Schedulers.trampoline());
    }

    @Test
    void filtersUnavailableAndTransformsAvailableProducts() {
        List<ProcessingIssue> issues = new ArrayList<>();
        TestSubscriber<ProductView> subscriber = service.process(
                Flowable.just(
                        product(1, "Поставщик", "Доступный", "12.50", 4),
                        product(1, "Поставщик", "Нет на складе", "99.00", 0)),
                issues::add).test();

        subscriber.assertComplete().assertNoErrors().assertValueCount(1);
        ProductView result = subscriber.values().getFirst();
        assertEquals("Доступный", result.product());
        assertEquals("50.00", result.totalCost().toPlainString());
        assertTrue(issues.isEmpty());
    }

    @Test
    void handlesInvalidItemWithoutTerminatingStream() {
        List<ProcessingIssue> issues = new ArrayList<>();
        TestSubscriber<ProductView> subscriber = service.process(
                Flowable.just(
                        product(1, "Поставщик", "Плохой", "-1.00", 1),
                        product(2, "Другой", "Хороший", "20.00", 2)),
                issues::add).test();

        subscriber.assertComplete().assertNoErrors().assertValueCount(1);
        assertEquals("Хороший", subscriber.values().getFirst().product());
        assertEquals(1, issues.size());
        assertTrue(issues.getFirst().message().contains("price"));
    }

    @Test
    void groupsAndReducesTotalsBySupplier() {
        List<ProductView> views = service.process(
                Flowable.just(
                        product(1, "A", "Первый", "10.00", 2),
                        product(1, "A", "Второй", "5.00", 3),
                        product(2, "B", "Третий", "7.50", 4)),
                issue -> {
                    throw new AssertionError(issue);
                }).toList().blockingGet();

        List<SupplierTotal> totals = service
                .totalBySupplier(Flowable.fromIterable(views))
                .blockingGet();

        assertEquals(2, totals.size());
        assertEquals(new SupplierTotal("A", new BigDecimal("35.00")), totals.get(0));
        assertEquals(new SupplierTotal("B", new BigDecimal("30.00")), totals.get(1));
    }

    @Test
    void reportsEveryKindOfValidationError() {
        List<ProcessingIssue> issues = new ArrayList<>();
        service.process(
                Flowable.just(
                        product(0, "A", "Товар", "1.00", 1),
                        product(1, " ", "Товар", "1.00", 1),
                        product(2, "B", " ", "1.00", 1),
                        product(3, "C", "Товар", "1.00", -1)),
                issues::add).test().assertComplete().assertNoValues().assertNoErrors();

        assertEquals(4, issues.size());
    }

    private SupplierProduct product(
            long supplierId, String supplierName, String name, String price, int quantity) {
        return new SupplierProduct(
                supplierId, supplierName, name, new BigDecimal(price), quantity);
    }
}
