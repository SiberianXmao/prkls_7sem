package ru.pr1.task2;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExecutorComparisonTest {
    @Test
    void bothExecutorsProcessTheSameTasks() throws Exception {
        List<BlockingTaskSpec> specs = ExecutorComparison.createSpecs(30, 1, 2, 27);
        List<BlockingTaskResult> fixed = ExecutorComparison.runFixed(specs, 4);
        List<BlockingTaskResult> virtual = ExecutorComparison.runVirtual(specs);

        assertEquals(fixed, virtual);
        assertEquals(specs.size(), fixed.size());
    }
}
