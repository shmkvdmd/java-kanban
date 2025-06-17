package models.manager;

import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private HistoryManager historyManager;

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void shouldReturnEmptyHistoryWhenNoTasks() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty(), "История должна быть пустой");
    }

    @Test
    void shouldNotDuplicateTasksInHistory() {
        LocalDateTime startTime = LocalDateTime.now();
        Task task = new Task("Task1", "Desc", TaskStatus.NEW, startTime,
                Duration.ofMinutes(30));
        task.setId(1);
        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дублирование задач в истории");
        assertEquals(task, history.get(0), "Задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromBeginning() {
        LocalDateTime startTime1 = LocalDateTime.now();
        LocalDateTime startTime2 = LocalDateTime.now().plusHours(1);
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, startTime1,
                Duration.ofMinutes(30));
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, startTime2,
                Duration.ofMinutes(30));
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не удалена из начала");
        assertEquals(task2, history.get(0), "Неверная задача осталась");
    }

    @Test
    void shouldRemoveTaskFromMiddle() {
        LocalDateTime startTime1 = LocalDateTime.now();
        LocalDateTime startTime2 = LocalDateTime.now().plusHours(1);
        LocalDateTime startTime3 = LocalDateTime.now().plusHours(2);
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, startTime1,
                Duration.ofMinutes(30));
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, startTime2,
                Duration.ofMinutes(30));
        task2.setId(2);
        Task task3 = new Task("Task3", "Desc", TaskStatus.NEW, startTime3,
                Duration.ofMinutes(30));
        task3.setId(3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Задача не удалена из середины");
        assertEquals(task1, history.get(0), "Неверная первая задача");
        assertEquals(task3, history.get(1), "Неверная последняя задача");
    }

    @Test
    void shouldRemoveTaskFromEnd() {
        LocalDateTime startTime1 = LocalDateTime.now();
        LocalDateTime startTime2 = LocalDateTime.now().plusHours(1);
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, startTime1,
                Duration.ofMinutes(30));
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW, startTime2,
                Duration.ofMinutes(30));
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не удалена с конца");
        assertEquals(task1, history.get(0), "Неверная задача осталась");
    }
}