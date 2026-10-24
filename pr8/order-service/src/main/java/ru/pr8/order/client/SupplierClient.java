package ru.pr8.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "supplier-service")
public interface SupplierClient {
    @GetMapping("/internal/products/{id}/availability")
    ProductSnapshot checkAvailability(@PathVariable("id") long productId, @RequestParam("quantity") int quantity);
}
