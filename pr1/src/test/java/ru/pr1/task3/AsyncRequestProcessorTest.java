package ru.pr1.task3;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AsyncRequestProcessorTest {
    @Test
    void calculatesValidRequest() {
        try (AsyncRequestProcessor processor = new AsyncRequestProcessor(2, Duration.ofSeconds(1))) {
            RequestResult result = processor.process(
                    new Request("REQ-1", 3, new BigDecimal("100.00"), 10)).join();

            assertTrue(result.success());
            assertEquals("REQ-1", result.requestId());
            assertEquals("Итоговая стоимость: 270.00", result.message());
        }
    }

    @Test
    void turnsValidationErrorIntoReadableResult() {
        try (AsyncRequestProcessor processor = new AsyncRequestProcessor(2, Duration.ofSeconds(1))) {
            RequestResult result = processor.process(
                    new Request("BAD-1", 0, BigDecimal.TEN, 0)).join();

            assertFalse(result.success());
            assertEquals("BAD-1", result.requestId());
            assertTrue(result.message().contains("Количество"));
        }
    }
}
