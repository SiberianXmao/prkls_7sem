package ru.pr8.supplier.service;

import org.junit.jupiter.api.Test;
import ru.pr8.supplier.domain.Product;
import ru.pr8.supplier.repository.ProductRepository;
import ru.pr8.supplier.repository.SupplierRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogServiceTest {
    private final SupplierRepository supplierRepository = mock(SupplierRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);
    private final CatalogService service = new CatalogService(supplierRepository, productRepository);

    @Test
    void returnsProductAvailability() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(
                new Product(1L, 2L, "SKU", "Keyboard", new BigDecimal("100.00"), 5)));

        var result = service.checkAvailability(1L, 3);

        assertThat(result.requestedQuantity()).isEqualTo(3);
        assertThat(result.unitPrice()).isEqualByComparingTo("100.00");
    }

    @Test
    void rejectsInsufficientStock() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(
                new Product(1L, 2L, "SKU", "Keyboard", new BigDecimal("100.00"), 1)));

        assertThatThrownBy(() -> service.checkAvailability(1L, 2))
                .isInstanceOf(CatalogException.class)
                .hasMessageContaining("Not enough");
    }
}
