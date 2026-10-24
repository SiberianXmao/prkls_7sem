package ru.pr8.delivery.api;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr8.delivery.service.DeliveryService;

@RestController
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/api/deliveries")
    Flux<DeliveryResponse> all() {
        return deliveryService.findAll();
    }

    @GetMapping("/api/deliveries/{id}")
    Mono<DeliveryResponse> byId(@PathVariable long id) {
        return deliveryService.findById(id);
    }

    @PutMapping("/api/deliveries/{id}/status")
    Mono<DeliveryResponse> updateStatus(
            @PathVariable long id,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return deliveryService.updateStatus(id, request.status());
    }

    @GetMapping(value = "/api/orders/{orderId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    Flux<ServerSentEvent<DeliveryEventResponse>> events(@PathVariable long orderId) {
        return deliveryService.stream(orderId)
                .map(event -> ServerSentEvent.<DeliveryEventResponse>builder()
                        .id(event.orderId() + "-" + event.occurredAt().toEpochMilli())
                        .event("order-status")
                        .data(event)
                        .build());
    }
}
