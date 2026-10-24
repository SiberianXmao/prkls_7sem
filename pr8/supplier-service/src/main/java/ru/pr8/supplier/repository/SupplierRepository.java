package ru.pr8.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pr8.supplier.domain.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
