package ru.pr8.supplier.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pr8.supplier.service.CatalogService;

import java.util.List;

@RestController
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/api/suppliers")
    public List<SupplierResponse> suppliers() {
        return catalogService.findSuppliers();
    }

    @GetMapping("/api/products")
    public List<ProductResponse> products() {
        return catalogService.findProducts();
    }

    @GetMapping("/api/products/{id}")
    public ProductResponse product(@PathVariable long id) {
        return catalogService.findProduct(id);
    }

    @GetMapping("/internal/products/{id}/availability")
    public ProductAvailabilityResponse availability(@PathVariable long id, @RequestParam int quantity) {
        return catalogService.checkAvailability(id, quantity);
    }
}
