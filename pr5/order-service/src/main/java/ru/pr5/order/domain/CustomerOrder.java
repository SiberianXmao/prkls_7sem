package ru.pr5.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customer_orders", schema = "orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 300)
    private String deliveryAddress;

    @Column(nullable = false, length = 160)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected CustomerOrder() {
    }

    public static CustomerOrder create(
            String customerName,
            String deliveryAddress,
            String productName,
            int quantity,
            BigDecimal totalAmount,
            Instant now) {
        CustomerOrder order = new CustomerOrder();
        order.customerName = customerName;
        order.deliveryAddress = deliveryAddress;
        order.productName = productName;
        order.quantity = quantity;
        order.totalAmount = totalAmount;
        order.status = OrderStatus.NEW;
        order.createdAt = now;
        order.updatedAt = now;
        return order;
    }

    public void changeStatus(OrderStatus newStatus, Instant now) {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Отменённый заказ нельзя изменить");
        }
        if (status == OrderStatus.CONFIRMED && newStatus == OrderStatus.NEW) {
            throw new IllegalStateException("Подтверждённый заказ нельзя вернуть в статус NEW");
        }
        status = newStatus;
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
