package ru.pr5.order.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pr5.order.api.CreateOrderRequest;
import ru.pr5.order.api.OrderResponse;
import ru.pr5.order.domain.CustomerOrder;
import ru.pr5.order.domain.OrderStatus;
import ru.pr5.order.repository.CustomerOrderRepository;
import ru.pr5.order.support.InstanceInfo;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private final CustomerOrderRepository repository;
    private final InstanceInfo instanceInfo;
    private final Clock clock;

    public OrderService(
            CustomerOrderRepository repository,
            InstanceInfo instanceInfo,
            Clock clock) {
        this.repository = repository;
        this.instanceInfo = instanceInfo;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Instant now = clock.instant();
        CustomerOrder order = CustomerOrder.create(
                request.customerName().trim(),
                request.deliveryAddress().trim(),
                request.productName().trim(),
                request.quantity(),
                request.totalAmount(),
                now);
        return toResponse(repository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(long orderId) {
        return toResponse(findEntity(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse changeStatus(long orderId, OrderStatus status) {
        CustomerOrder order = findEntity(orderId);
        try {
            order.changeStatus(status, clock.instant());
        } catch (IllegalStateException exception) {
            throw new InvalidOrderStateException(exception.getMessage());
        }
        return toResponse(order);
    }

    private CustomerOrder findEntity(long orderId) {
        return repository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getDeliveryAddress(),
                order.getProductName(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                instanceInfo.id());
    }
}
