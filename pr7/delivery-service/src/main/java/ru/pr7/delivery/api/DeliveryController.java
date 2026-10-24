package ru.pr7.delivery.api;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.pr7.delivery.domain.DeliveryStatus;
import ru.pr7.delivery.service.DeliveryService;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public Flux<DeliveryResponse> findAll(
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) BigDecimal minPrice) {
        return deliveryService.findAll(status, minPrice);
    }

    @GetMapping("/{deliveryId}")
    public Mono<DeliveryResponse> findById(@PathVariable long deliveryId) {
        return deliveryService.findById(deliveryId);
    }

    @PostMapping
    public Mono<ResponseEntity<DeliveryResponse>> create(
            @Valid @RequestBody CreateDeliveryRequest request) {
        return deliveryService.create(request)
                .map(delivery -> ResponseEntity
                        .created(URI.create("/api/deliveries/" + delivery.id()))
                        .body(delivery));
    }

    @PutMapping("/{deliveryId}")
    public Mono<DeliveryResponse> update(
            @PathVariable long deliveryId,
            @Valid @RequestBody UpdateDeliveryRequest request) {
        return deliveryService.update(deliveryId, request);
    }

    @DeleteMapping("/{deliveryId}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable long deliveryId) {
        return deliveryService.delete(deliveryId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping(value = "/{deliveryId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DeliveryEvent>> stream(@PathVariable long deliveryId) {
        return deliveryService.stream(deliveryId)
                .map(event -> ServerSentEvent.<DeliveryEvent>builder()
                        .id(event.deliveryId() + "-" + event.occurredAt().toEpochMilli())
                        .event(event.type().toLowerCase())
                        .data(event)
                        .build());
    }
}
