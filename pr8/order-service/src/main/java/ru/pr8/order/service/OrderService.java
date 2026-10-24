package ru.pr8.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pr8.order.api.CreateOrderRequest;
import ru.pr8.order.api.OrderResponse;
import ru.pr8.order.client.ProductSnapshot;
import ru.pr8.order.client.SupplierCatalogGateway;
import ru.pr8.order.domain.CustomerOrder;
import ru.pr8.order.event.OrderEventPublisher;
import ru.pr8.order.repository.CustomerOrderRepository;

import java.util.List;

@Service
public class OrderService {
    private final CustomerOrderRepository repository;
    private final SupplierCatalogGateway supplierCatalog;
    private final OrderEventPublisher eventPublisher;
    private final String instanceId;

    public OrderService(
            CustomerOrderRepository repository,
            SupplierCatalogGateway supplierCatalog,
            OrderEventPublisher eventPublisher) {
        this.repository = repository;
        this.supplierCatalog = supplierCatalog;
        this.eventPublisher = eventPublisher;
        this.instanceId = System.getenv().getOrDefault("HOSTNAME", "local");
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request, String requestId) {
        return repository.findByClientRequestId(request.clientRequestId())
                .map(order -> OrderResponse.from(order, instanceId))
                .orElseGet(() -> createNew(request, requestId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return repository.findAll().stream().map(order -> OrderResponse.from(order, instanceId)).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(long id) {
        CustomerOrder order = repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return OrderResponse.from(order, instanceId);
    }

    private OrderResponse createNew(CreateOrderRequest request, String requestId) {
        ProductSnapshot product = supplierCatalog.requireAvailable(request.productId(), request.quantity());
        CustomerOrder order = new CustomerOrder(
                request.clientRequestId(),
                product.productId(),
                product.productName(),
                product.unitPrice(),
                request.quantity(),
                request.customerName(),
                request.deliveryAddress(),
                requestId);
        CustomerOrder saved = repository.save(order);
        eventPublisher.publish(saved);
        return OrderResponse.from(saved, instanceId);
    }
}
