package ru.pr1.task4;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileProcessingSimulationTest {
    @Test
    void processesEveryTaskExactlyOnce() throws Exception {
        List<ScheduledFileTask> tasks = FileProcessingSimulation.createTasks(30, 27, 0, 0);
        ProcessingStatsSnapshot stats = FileProcessingSimulation.run(tasks, 3, 4, 0);

        stats.verifyInvariants();
        assertEquals(30, stats.generated());
        assertEquals(30, stats.processed());
        assertEquals(10, stats.count(FileType.XML));
        assertEquals(10, stats.count(FileType.JSON));
        assertEquals(10, stats.count(FileType.XLS));
    }
}
