package manager;

import ru.common.models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.HistoryManager;
import ru.common.manager.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static manager.utility.MockData.*;
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
        Task task = createTask();
        task.setId(1);
        historyManager.add(task);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Дублирование задач в истории");
        assertEquals(task, history.get(0), "Задача не совпадает");
    }

    @Test
    void shouldRemoveTaskFromBeginning() {
        LocalDateTime dateTime = LocalDateTime.now();
        Task task1 = createTask(dateTime, Duration.ofMinutes(30));
        task1.setId(1);
        Task task2 = createTask(dateTime.plusHours(1), Duration.ofMinutes(30));
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
        LocalDateTime dateTime = LocalDateTime.now();
        Task task1 = createTask(dateTime, Duration.ofMinutes(30));
        task1.setId(1);
        Task task2 = createTask(dateTime.plusHours(1), Duration.ofMinutes(30));
        task2.setId(2);
        Task task3 = createTask(dateTime.plusHours(2), Duration.ofMinutes(30));
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
        LocalDateTime dateTime = LocalDateTime.now();
        Task task1 = createTask(dateTime, Duration.ofMinutes(30));
        task1.setId(1);
        Task task2 = createTask(dateTime.plusHours(1), Duration.ofMinutes(30));
        task2.setId(2);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(2);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "Задача не удалена с конца");
        assertEquals(task1, history.get(0), "Неверная задача осталась");
    }
}