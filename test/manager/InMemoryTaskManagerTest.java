package manager;

import ru.common.models.tasks.Task;
import org.junit.jupiter.api.Test;
import ru.common.manager.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static manager.utility.MockData.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        LocalDateTime dateTime = LocalDateTime.now();
        Task task1 = createTask(dateTime, Duration.ofMinutes(30));
        Task task2 = createTask(dateTime.plusHours(1), Duration.ofMinutes(30));
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(2, prioritizedTasks.size(), "Некорректное количество приоритетных задач");
        assertTrue(prioritizedTasks.get(0).getStartTime().isBefore(prioritizedTasks.get(1).getStartTime()),
                "Задачи не отсортированы по времени");
    }

    @Test
    void shouldReturnEmptyPrioritizedTasksWhenNoTasks() {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertTrue(prioritizedTasks.isEmpty(), "Приоритетный список должен быть пустым");
    }
}