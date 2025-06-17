package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;

    protected abstract T createManager();

    @BeforeEach
    void beforeEach() {
        taskManager = createManager();
    }

    @Test
    void shouldSetEpicStatusNewWhenAllSubtasksNew() {
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Sub1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
        Subtask subtask2 = new Subtask("Sub2", "Desc", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.NEW, updatedEpic.getTaskStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void shouldSetEpicStatusDoneWhenAllSubtasksDone() {
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Sub1", "Desc", TaskStatus.DONE, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
        Subtask subtask2 = new Subtask("Sub2", "Desc", TaskStatus.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, updatedEpic.getTaskStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void shouldSetEpicStatusInProgressWhenSubtasksNewAndDone() {
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Sub1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
        Subtask subtask2 = new Subtask("Sub2", "Desc", TaskStatus.DONE,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getTaskStatus(),
                "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void shouldSetEpicStatusInProgressWhenSubtasksInProgress() {
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("Sub1", "Desc", TaskStatus.IN_PROGRESS,
                LocalDateTime.now(), Duration.ofMinutes(30), epicId);
        Subtask subtask2 = new Subtask("Sub2", "Desc", TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getTaskStatus(),
                "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void shouldThrowExceptionWhenIntersection() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(60));
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW,
                LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(60));
        taskManager.addTask(task1);
        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2),
                "Должно быть исключение из-за пересечения");
    }

    @Test
    void shouldNotThrowExceptionWhenNoIntersection() {
        Task task1 = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(60));
        Task task2 = new Task("Task2", "Desc", TaskStatus.NEW,
                LocalDateTime.now().plusHours(2), Duration.ofMinutes(60));
        taskManager.addTask(task1);
        assertDoesNotThrow(() -> taskManager.addTask(task2),
                "Не должно быть исключения при отсутствии пересечения");
    }

    @Test
    void shouldLinkSubtaskToEpic() {
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Sub1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
        int subtaskId = taskManager.addSubtask(subtask);
        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(1, subtasks.size(), "Подзадача не добавлена в эпик");
        assertEquals(subtask, subtasks.get(0), "Подзадача не совпадает");
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtaskChanges() {
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Sub1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask);
        subtask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, updatedEpic.getTaskStatus(), "Статус эпика не обновился");
    }
}