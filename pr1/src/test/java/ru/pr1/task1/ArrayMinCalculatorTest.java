package ru.pr1.task1;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayMinCalculatorTest {
    @Test
    void allImplementationsReturnTheSameMinimum() throws Exception {
        int[] values = new SplittableRandom(27).ints(10_003).toArray();
        int expected = ArrayMinCalculator.sequentialMin(values);

        assertEquals(expected, ArrayMinCalculator.executorMin(values, 4));
        assertEquals(expected, ArrayMinCalculator.forkJoinMin(values, 128, 4));
    }

    @Test
    void handlesMoreWorkersThanElements() throws Exception {
        int[] values = {7, -10, 4};
        assertEquals(-10, ArrayMinCalculator.executorMin(values, 20));
    }

    @Test
    void rejectsEmptyArray() {
        assertThrows(IllegalArgumentException.class,
                () -> ArrayMinCalculator.sequentialMin(new int[0]));
    }
}
