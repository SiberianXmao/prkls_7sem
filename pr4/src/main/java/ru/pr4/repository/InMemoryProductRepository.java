package ru.pr4.repository;

import ru.pr4.model.PriceUpdate;
import ru.pr4.model.SupplierProduct;
import ru.pr4.model.UpdateAcknowledgement;
import ru.pr4.model.ViewEvent;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

public final class InMemoryProductRepository {
    private final ConcurrentSkipListMap<Long, SupplierProduct> products =
            new ConcurrentSkipListMap<>();
    private final CopyOnWriteArrayList<ViewEvent> viewEvents = new CopyOnWriteArrayList<>();

    public InMemoryProductRepository(List<SupplierProduct> initialProducts) {
        initialProducts.forEach(product -> {
            SupplierProduct previous = products.putIfAbsent(product.productId(), product);
            if (previous != null) {
                throw new IllegalArgumentException("Повторный productId: " + product.productId());
            }
        });
    }

    public static InMemoryProductRepository sample() {
        return new InMemoryProductRepository(List.of(
                product(1, 101, "Северный склад", "Механическая клавиатура", "7490.00", 18),
                product(2, 101, "Северный склад", "Беспроводная мышь", "3290.50", 35),
                product(3, 202, "ТехноИмпорт", "Монитор 27 дюймов", "28990.00", 12),
                product(4, 202, "ТехноИмпорт", "USB-C док-станция", "11990.00", 21),
                product(5, 303, "Офис, Сервис", "Картридж для принтера", "6590.00", 14)
        ));
    }

    public Optional<SupplierProduct> findById(long productId) {
        return Optional.ofNullable(products.get(productId));
    }

    public List<SupplierProduct> findAll() {
        return List.copyOf(products.values());
    }

    public void recordView(ViewEvent event) {
        if (!products.containsKey(event.productId())) {
            throw new ProductNotFoundException(event.productId());
        }
        viewEvents.add(event);
    }

    public List<ViewEvent> viewEvents() {
        return List.copyOf(viewEvents);
    }

    public UpdateAcknowledgement updatePrice(PriceUpdate update) {
        AtomicReference<BigDecimal> oldPrice = new AtomicReference<>();
        SupplierProduct updated = products.compute(update.productId(), (id, current) -> {
            if (current == null) {
                throw new ProductNotFoundException(id);
            }
            oldPrice.set(current.price());
            return current.withPrice(update.newPrice());
        });
        return new UpdateAcknowledgement(
                update.productId(), oldPrice.get(), updated.price(), "updated");
    }

    private static SupplierProduct product(
            long productId,
            long supplierId,
            String supplierName,
            String product,
            String price,
            int quantity) {
        return new SupplierProduct(
                productId, supplierId, supplierName, product, new BigDecimal(price), quantity);
    }
}
