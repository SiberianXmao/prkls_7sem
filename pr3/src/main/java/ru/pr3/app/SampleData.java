package ru.pr3.app;

import ru.pr3.model.SupplierProduct;

import java.math.BigDecimal;
import java.util.List;

final class SampleData {
    private SampleData() {
    }

    static List<SupplierProduct> batch() {
        return List.of(
                product(101, "Северный склад", "Механическая клавиатура", "7490.00", 18),
                product(101, "Северный склад", "Беспроводная мышь", "3290.50", 0),
                product(202, "ТехноИмпорт", "Монитор 27 дюймов", "28990.00", 12),
                product(202, "ТехноИмпорт", "USB-C док-станция", "11990.00", 21),
                product(303, "Офис, Сервис", "Бумага A4", "429.90", 240),
                product(303, "Офис, Сервис", "Картридж для принтера", "6590.00", 14),
                product(404, "Ошибочный поставщик", "Товар с плохой ценой", "-10.00", 2),
                product(505, "Поставщик без товара", " ", "100.00", 1)
        );
    }

    static List<SupplierProduct> timed() {
        return List.of(
                product(601, "Онлайн поставщик", "SSD 1 ТБ", "8990.00", 5),
                product(601, "Онлайн поставщик", "Оперативная память", "6490.00", 8),
                product(602, "Поток данных", "Сетевой адаптер", "2190.00", 11)
        );
    }

    private static SupplierProduct product(
            long supplierId,
            String supplierName,
            String product,
            String price,
            int quantity) {
        return new SupplierProduct(
                supplierId, supplierName, product, new BigDecimal(price), quantity);
    }
}
