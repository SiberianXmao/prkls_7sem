package ru.pr5.delivery.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.pr5.delivery.service.DeliveryService;
import ru.pr5.delivery.support.InstanceInfo;

import java.net.URI;
import java.time.Clock;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final InstanceInfo instanceInfo;
    private final Clock clock;

    public DeliveryController(
            DeliveryService deliveryService,
            InstanceInfo instanceInfo,
            Clock clock) {
        this.deliveryService = deliveryService;
        this.instanceInfo = instanceInfo;
        this.clock = clock;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(
            @Valid @RequestBody CreateDeliveryRequest request) {
        DeliveryResponse response = deliveryService.create(request);
        return ResponseEntity.created(URI.create("/api/deliveries/" + response.id())).body(response);
    }

    @GetMapping("/{deliveryId}")
    public DeliveryResponse findById(@PathVariable long deliveryId) {
        return deliveryService.findById(deliveryId);
    }

    @GetMapping
    public List<DeliveryResponse> findAll() {
        return deliveryService.findAll();
    }

    @PatchMapping("/{deliveryId}/status")
    public DeliveryResponse changeStatus(
            @PathVariable long deliveryId,
            @Valid @RequestBody UpdateDeliveryStatusRequest request) {
        return deliveryService.changeStatus(deliveryId, request.status());
    }

    @GetMapping("/instance")
    public InstanceResponse instance() {
        return new InstanceResponse("delivery-service", instanceInfo.id(), clock.instant());
    }
}
