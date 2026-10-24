package ru.pr7.order.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("customer_orders")
public class CustomerOrder {
    @Id
    private Long id;

    @Column("customer_name")
    private String customerName;

    @Column("delivery_address")
    private String deliveryAddress;

    @Column("total_amount")
    private BigDecimal totalAmount;

    private OrderStatus status;

    public CustomerOrder() {
    }

    public CustomerOrder(
            Long id,
            String customerName,
            String deliveryAddress,
            BigDecimal totalAmount,
            OrderStatus status) {
        this.id = id;
        this.customerName = customerName;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
