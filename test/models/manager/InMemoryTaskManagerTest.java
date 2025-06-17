package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return new InMemoryTaskManager();
    }

    @Test
    void shouldReturnPrioritizedTasks() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30));
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, LocalDateTime.now().plusHours(1),
                Duration.ofMinutes(30));
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