package ru.pr5.delivery.service;

import org.springframework.data.domain.Sort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pr5.delivery.api.CreateDeliveryRequest;
import ru.pr5.delivery.api.DeliveryResponse;
import ru.pr5.delivery.client.OrderServiceClient;
import ru.pr5.delivery.client.OrderSnapshot;
import ru.pr5.delivery.domain.Delivery;
import ru.pr5.delivery.domain.DeliveryStatus;
import ru.pr5.delivery.repository.DeliveryRepository;
import ru.pr5.delivery.support.InstanceInfo;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class DeliveryService {
    private final DeliveryRepository repository;
    private final OrderServiceClient orderServiceClient;
    private final InstanceInfo instanceInfo;
    private final Clock clock;

    public DeliveryService(
            DeliveryRepository repository,
            OrderServiceClient orderServiceClient,
            InstanceInfo instanceInfo,
            Clock clock) {
        this.repository = repository;
        this.orderServiceClient = orderServiceClient;
        this.instanceInfo = instanceInfo;
        this.clock = clock;
    }

    @Transactional
    public DeliveryResponse create(CreateDeliveryRequest request) {
        if (repository.findByOrderId(request.orderId()).isPresent()) {
            throw new DuplicateDeliveryException(request.orderId());
        }

        OrderSnapshot order = orderServiceClient.findById(request.orderId());
        if ("CANCELLED".equals(order.status())) {
            throw new InvalidDeliveryStateException("Для отменённого заказа нельзя создать доставку");
        }

        Instant now = clock.instant();
        Delivery delivery = Delivery.create(
                order.id(),
                order.customerName(),
                order.deliveryAddress(),
                request.courierName().trim(),
                now.plus(Duration.ofDays(2)),
                now);
        try {
            return toResponse(repository.saveAndFlush(delivery), order.servedBy());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateDeliveryException(request.orderId());
        }
    }

    @Transactional(readOnly = true)
    public DeliveryResponse findById(long deliveryId) {
        return toResponse(findEntity(deliveryId), null);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> findAll() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(delivery -> toResponse(delivery, null))
                .toList();
    }

    @Transactional
    public DeliveryResponse changeStatus(long deliveryId, DeliveryStatus status) {
        Delivery delivery = findEntity(deliveryId);
        try {
            delivery.changeStatus(status, clock.instant());
        } catch (IllegalStateException exception) {
            throw new InvalidDeliveryStateException(exception.getMessage());
        }
        return toResponse(delivery, null);
    }

    private Delivery findEntity(long deliveryId) {
        return repository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }

    private DeliveryResponse toResponse(Delivery delivery, String orderServedBy) {
        return new DeliveryResponse(
                delivery.getId(),
                delivery.getOrderId(),
                delivery.getCustomerName(),
                delivery.getDeliveryAddress(),
                delivery.getCourierName(),
                delivery.getStatus(),
                delivery.getEstimatedDeliveryAt(),
                delivery.getCreatedAt(),
                delivery.getUpdatedAt(),
                instanceInfo.id(),
                orderServedBy);
    }
}
