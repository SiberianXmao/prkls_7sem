package ru.pr8.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientRequestId;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal totalPrice;
    private String customerName;
    private String deliveryAddress;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String requestId;
    private Instant createdAt;

    protected CustomerOrder() {
    }

    public CustomerOrder(
            String clientRequestId,
            Long productId,
            String productName,
            BigDecimal unitPrice,
            int quantity,
            String customerName,
            String deliveryAddress,
            String requestId) {
        this.clientRequestId = clientRequestId;
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.status = OrderStatus.CONFIRMED;
        this.requestId = requestId;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getClientRequestId() { return clientRequestId; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getCustomerName() { return customerName; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public OrderStatus getStatus() { return status; }
    public String getRequestId() { return requestId; }
    public Instant getCreatedAt() { return createdAt; }
}
