package ru.pr3.reactive;

import org.junit.jupiter.api.Test;
import ru.pr3.model.BackpressureReport;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackpressureDemoTest {
    @Test
    void dropsOldestItemsWhenProducerIsFaster() {
        BackpressureReport report = new BackpressureDemo()
                .run(100, 4, Duration.ofMillis(2));

        assertEquals(100, report.produced());
        assertTrue(report.dropped() > 0);
        assertEquals(report.produced(), report.consumed() + report.dropped());
    }
}
