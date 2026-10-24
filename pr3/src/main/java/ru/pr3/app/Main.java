package ru.pr3.app;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.pr3.model.BackpressureReport;
import ru.pr3.model.ProcessingIssue;
import ru.pr3.model.ProductView;
import ru.pr3.model.SupplierTotal;
import ru.pr3.reactive.BackpressureDemo;
import ru.pr3.reactive.ReactiveProductService;
import ru.pr3.reactive.TimedDataSource;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        try {
            runBatchPipeline();
            runTimedPipeline();
            runBackpressureDemo();
        } finally {
            Schedulers.shutdown();
        }
    }

    private static void runBatchPipeline() {
        System.out.println("=== 1. filter / map / flatMap / Scheduler ===");
        ReactiveProductService service = new ReactiveProductService();
        List<ProcessingIssue> issues = new CopyOnWriteArrayList<>();

        Flowable<ProductView> pipeline = service.process(
                        Flowable.fromIterable(SampleData.batch())
                                .doOnNext(item -> printThread("Источник", item.product())),
                        issues::add)
                .doOnNext(view -> printThread("Обработка", view.product()));

        List<ProductView> availableProducts = pipeline.toList().blockingGet();
        availableProducts.forEach(product -> System.out.printf(
                "AVAILABLE | %s | %s | %d шт. | стоимость %s | thread=%s%n",
                product.supplierName(), product.product(), product.quantity(),
                product.totalCost().toPlainString(), product.processedByThread()));

        System.out.println("\n=== 2. groupBy / reduce ===");
        List<SupplierTotal> totals = service
                .totalBySupplier(Flowable.fromIterable(availableProducts))
                .blockingGet();
        totals.forEach(total -> System.out.printf(
                "%s -> %s%n", total.supplierName(), total.totalCost().toPlainString()));

        System.out.println("\n=== 3. Обработанные ошибки ===");
        issues.forEach(issue -> System.out.printf(
                "supplierId=%d, product=%s, error=%s%n",
                issue.supplierId(), issue.product(), issue.message()));
        System.out.printf("Цепочка завершилась штатно: товаров=%d, ошибок=%d%n%n",
                availableProducts.size(), issues.size());
    }

    private static void runTimedPipeline() {
        System.out.println("=== 4. Поступление данных во времени ===");
        ReactiveProductService service = new ReactiveProductService();
        service.process(
                        TimedDataSource.atInterval(
                                        SampleData.timed(),
                                        Duration.ofMillis(150),
                                        Schedulers.computation())
                                .doOnNext(item -> printThread("Новое событие", item.product())),
                        issue -> System.out.println("Ошибка timed-потока: " + issue))
                .blockingForEach(view -> System.out.printf(
                        "Автоматически обработан: %s, total=%s, thread=%s%n",
                        view.product(), view.totalCost().toPlainString(),
                        view.processedByThread()));
        System.out.println();
    }

    private static void runBackpressureDemo() {
        System.out.println("=== 5. Backpressure: DROP_OLDEST ===");
        BackpressureReport report = new BackpressureDemo()
                .run(300, 8, Duration.ofMillis(5));
        System.out.printf("Produced: %d%nConsumed: %d%nDropped: %d%nBuffer capacity: %d%n",
                report.produced(), report.consumed(), report.dropped(), report.bufferCapacity());
    }

    private static void printThread(String stage, String value) {
        System.out.printf("%-12s | %-30s | thread=%s%n",
                stage, value, Thread.currentThread().getName());
    }
}
