package ru.pr8.supplier.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long supplierId;
    private String sku;
    private String name;
    private BigDecimal price;
    private int availableQuantity;

    protected Product() {
    }

    public Product(Long id, Long supplierId, String sku, String name, BigDecimal price, int availableQuantity) {
        this.id = id;
        this.supplierId = supplierId;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.availableQuantity = availableQuantity;
    }

    public Long getId() {
        return id;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
