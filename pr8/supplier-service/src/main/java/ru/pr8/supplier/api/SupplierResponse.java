package ru.pr8.supplier.api;

import ru.pr8.supplier.domain.Supplier;

public record SupplierResponse(Long id, String name, boolean active) {
    public static SupplierResponse from(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.isActive());
    }
}
