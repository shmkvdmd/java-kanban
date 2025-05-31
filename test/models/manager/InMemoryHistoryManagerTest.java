package models.manager;

import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach() {
        taskManager = Managers.getDefault(Managers.getDefaultHistory());
    }

    @Test
    public void shouldBeUnique() {
        int id = taskManager.addTask(new Task("name1", "description", TaskStatus.NEW));
        taskManager.getTaskById(id);
        taskManager.getTaskById(id);
        taskManager.getTaskById(id);
        List<Task> historyList = taskManager.getHistoryManager().getHistory();
        int listSize = 1;
        assertEquals(listSize, historyList.size(), "В истории дублируются просмотры");
    }

    @Test
    public void shouldAddElementsToHistory() {
        int idFirst = taskManager.addTask(new Task("name1", "description", TaskStatus.NEW));
        int idSecond = taskManager.addTask(new Task("name2", "description", TaskStatus.NEW));
        int idThird = taskManager.addTask(new Task("name3", "description", TaskStatus.NEW));
        taskManager.getTaskById(idFirst);
        taskManager.getTaskById(idSecond);
        taskManager.getTaskById(idThird);
        List<Task> historyList = taskManager.getHistoryManager().getHistory();
        int listSize = 3;
        assertEquals(listSize, historyList.size(), "В историю не добавляются просмотры");
    }

    @Test
    public void shouldRemoveFromHistory() {
        int idFirst = taskManager.addTask(new Task("name1", "description", TaskStatus.NEW));
        int idSecond = taskManager.addTask(new Task("name2", "description", TaskStatus.NEW));
        taskManager.getTaskById(idFirst);
        taskManager.getTaskById(idSecond);
        taskManager.deleteTaskById(idFirst);
        List<Task> historyList = taskManager.getHistoryManager().getHistory();
        int listSize = 1;
        assertEquals(listSize, historyList.size(), "Просмотр сохраняется после удаления задачи");
    }

    @Test
    public void shouldNotBeNPE() {
        int id = taskManager.addTask(new Task("name1", "description", TaskStatus.NEW));
        taskManager.getTaskById(id + 2);
    }

    @Test
    public void shouldNotBeNPEWhenAddNull() {
        int id = taskManager.addTask(new Task("name1", "description", TaskStatus.NEW));
        int id2 = taskManager.addTask(null);
    }

    @Test
    void removeTaskShouldNotThrowNpeWhenSingleTask() {
        HistoryManager manager = taskManager.getHistoryManager();
        Task task = new Task("Test", "Description", TaskStatus.NEW);
        task.setId(1);
        manager.add(task);

        assertDoesNotThrow(
                () -> manager.remove(task.getId()),
                "Удаление единственной задачи вызвало NPE"
        );

        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой после удаления");
    }
}