package ru.pr8.supplier.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.pr8.supplier.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
