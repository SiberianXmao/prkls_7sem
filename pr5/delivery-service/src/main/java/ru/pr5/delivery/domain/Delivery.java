package ru.pr5.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "deliveries", schema = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false, length = 120)
    private String customerName;

    @Column(nullable = false, length = 300)
    private String deliveryAddress;

    @Column(nullable = false, length = 120)
    private String courierName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DeliveryStatus status;

    @Column(nullable = false)
    private Instant estimatedDeliveryAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Delivery() {
    }

    public static Delivery create(
            Long orderId,
            String customerName,
            String deliveryAddress,
            String courierName,
            Instant estimatedDeliveryAt,
            Instant now) {
        Delivery delivery = new Delivery();
        delivery.orderId = orderId;
        delivery.customerName = customerName;
        delivery.deliveryAddress = deliveryAddress;
        delivery.courierName = courierName;
        delivery.status = DeliveryStatus.CREATED;
        delivery.estimatedDeliveryAt = estimatedDeliveryAt;
        delivery.createdAt = now;
        delivery.updatedAt = now;
        return delivery;
    }

    public void changeStatus(DeliveryStatus newStatus, Instant now) {
        if (status == DeliveryStatus.DELIVERED || status == DeliveryStatus.CANCELLED) {
            throw new IllegalStateException("Завершённую доставку нельзя изменить");
        }
        if (status == DeliveryStatus.CREATED && newStatus == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException("Сначала переведите доставку в статус IN_TRANSIT");
        }
        status = newStatus;
        updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getCourierName() {
        return courierName;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Instant getEstimatedDeliveryAt() {
        return estimatedDeliveryAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
