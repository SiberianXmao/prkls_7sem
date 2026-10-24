package ru.pr8.supplier.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.pr8.supplier.api.ProductAvailabilityResponse;
import ru.pr8.supplier.api.ProductResponse;
import ru.pr8.supplier.api.SupplierResponse;
import ru.pr8.supplier.domain.Product;
import ru.pr8.supplier.repository.ProductRepository;
import ru.pr8.supplier.repository.SupplierRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    public CatalogService(SupplierRepository supplierRepository, ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
    }

    public List<SupplierResponse> findSuppliers() {
        return supplierRepository.findAll().stream().map(SupplierResponse::from).toList();
    }

    public List<ProductResponse> findProducts() {
        return productRepository.findAll().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse findProduct(long id) {
        return ProductResponse.from(requireProduct(id));
    }

    public ProductAvailabilityResponse checkAvailability(long id, int quantity) {
        if (quantity < 1) {
            throw new CatalogException(400, "Quantity must be positive");
        }
        Product product = requireProduct(id);
        if (product.getAvailableQuantity() < quantity) {
            throw new CatalogException(409, "Not enough product in stock");
        }
        return new ProductAvailabilityResponse(
                product.getId(),
                product.getSupplierId(),
                product.getName(),
                product.getPrice(),
                quantity,
                product.getAvailableQuantity());
    }

    private Product requireProduct(long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CatalogException(404, "Product " + id + " not found"));
    }
}
