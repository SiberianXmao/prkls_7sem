package ru.pr4.repository;

import org.junit.jupiter.api.Test;
import ru.pr4.model.PriceUpdate;
import ru.pr4.model.UpdateAcknowledgement;
import ru.pr4.model.ViewEvent;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryProductRepositoryTest {
    @Test
    void updatesPriceAtomicallyAndReturnsOldPrice() {
        InMemoryProductRepository repository = InMemoryProductRepository.sample();

        UpdateAcknowledgement acknowledgement = repository.updatePrice(
                new PriceUpdate(1, new BigDecimal("7000.00")));

        assertEquals(new BigDecimal("7490.00"), acknowledgement.oldPrice());
        assertEquals(new BigDecimal("7000.00"), acknowledgement.newPrice());
        assertEquals(new BigDecimal("7000.00"), repository.findById(1).orElseThrow().price());
    }

    @Test
    void rejectsEventsForUnknownProduct() {
        InMemoryProductRepository repository = InMemoryProductRepository.sample();

        assertThrows(ProductNotFoundException.class,
                () -> repository.recordView(new ViewEvent(999, "test")));
        assertThrows(ProductNotFoundException.class,
                () -> repository.updatePrice(new PriceUpdate(999, BigDecimal.ONE)));
    }
}
