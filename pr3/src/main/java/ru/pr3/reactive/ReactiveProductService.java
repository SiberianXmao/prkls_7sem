package ru.pr3.reactive;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import ru.pr3.model.ProcessingIssue;
import ru.pr3.model.ProductView;
import ru.pr3.model.SupplierProduct;
import ru.pr3.model.SupplierTotal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public final class ReactiveProductService {
    private final Scheduler sourceScheduler;
    private final Scheduler processingScheduler;

    public ReactiveProductService() {
        this(Schedulers.io(), Schedulers.computation());
    }

    public ReactiveProductService(Scheduler sourceScheduler, Scheduler processingScheduler) {
        this.sourceScheduler = Objects.requireNonNull(sourceScheduler, "sourceScheduler");
        this.processingScheduler = Objects.requireNonNull(processingScheduler, "processingScheduler");
    }


    public Flowable<ProductView> process(
            Flowable<SupplierProduct> source,
            Consumer<ProcessingIssue> issueSink) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(issueSink, "issueSink");

        return source
                .subscribeOn(sourceScheduler)
                .flatMap(product -> Flowable.fromCallable(() -> validate(product))
                        .onErrorResumeNext(error -> {
                            issueSink.accept(ProcessingIssue.from(product, error));
                            return Flowable.empty();
                        }))
                .filter(product -> product.quantity() > 0)
                .observeOn(processingScheduler)
                .map(this::transform);
    }

    public Single<List<SupplierTotal>> totalBySupplier(Flowable<ProductView> products) {
        return products
                .groupBy(ProductView::supplierName)
                .flatMapSingle(group -> group
                        .reduce(BigDecimal.ZERO,
                                (total, product) -> total.add(product.totalCost()))
                        .map(total -> new SupplierTotal(
                                group.getKey(), total.setScale(2, RoundingMode.HALF_UP))))
                .sorted(Comparator.comparing(SupplierTotal::supplierName))
                .toList();
    }

    private SupplierProduct validate(SupplierProduct product) {
        if (product.supplierId() <= 0) {
            throw new IllegalArgumentException("supplierId должен быть положительным");
        }
        requireText(product.supplierName(), "supplierName");
        requireText(product.product(), "product");
        if (product.price() == null || product.price().signum() < 0) {
            throw new IllegalArgumentException("price не может быть отрицательной");
        }
        if (product.quantity() < 0) {
            throw new IllegalArgumentException("quantity не может быть отрицательным");
        }
        return product;
    }

    private ProductView transform(SupplierProduct product) {
        BigDecimal total = product.price()
                .multiply(BigDecimal.valueOf(product.quantity()))
                .setScale(2, RoundingMode.HALF_UP);
        return new ProductView(
                product.supplierId(),
                product.supplierName().trim(),
                product.product().trim(),
                product.price(),
                product.quantity(),
                total,
                Thread.currentThread().getName());
    }

    private void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не может быть пустым");
        }
    }
}
