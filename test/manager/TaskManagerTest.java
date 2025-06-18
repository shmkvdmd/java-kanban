package manager;

import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static manager.utility.MockData.*;

import ru.common.manager.TaskManager;
import ru.common.models.tasks.status.TaskStatus;

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
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        LocalDateTime dateTime = LocalDateTime.now();
        Subtask subtask1 = createSubtask(dateTime, Duration.ofMinutes(30), epicId);
        Subtask subtask2 = createSubtask(dateTime.plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.NEW, updatedEpic.getTaskStatus(), "Статус эпика должен быть NEW");
    }

    @Test
    void shouldSetEpicStatusDoneWhenAllSubtasksDone() {
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        LocalDateTime dateTime = LocalDateTime.now();
        Subtask subtask1 = createSubtask("Subtask1", "description", TaskStatus.DONE, dateTime,
                Duration.ofMinutes(30), epicId);
        Subtask subtask2 = createSubtask("Subtask2", "Subtask2", TaskStatus.DONE,
                dateTime.plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, updatedEpic.getTaskStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void shouldSetEpicStatusInProgressWhenSubtasksNewAndDone() {
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        LocalDateTime dateTime = LocalDateTime.now();
        Subtask subtask1 = createSubtask(dateTime, Duration.ofMinutes(30), epicId);
        Subtask subtask2 = createSubtask("Subtask2", "Subtask2", TaskStatus.DONE,
                dateTime.plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getTaskStatus(),
                "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void shouldSetEpicStatusInProgressWhenSubtasksInProgress() {
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        LocalDateTime dateTime = LocalDateTime.now();
        Subtask subtask1 = createSubtask("Subtask1", "description", TaskStatus.IN_PROGRESS, dateTime,
                Duration.ofMinutes(30), epicId);
        Subtask subtask2 = createSubtask("Subtask2", "Subtask2", TaskStatus.IN_PROGRESS,
                dateTime.plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.IN_PROGRESS, updatedEpic.getTaskStatus(),
                "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
    void shouldThrowExceptionWhenIntersection() {
        LocalDateTime dateTime = LocalDateTime.now();
        Task task1 = createTask(dateTime, Duration.ofMinutes(60));
        Task task2 = createTask(dateTime.plusMinutes(30), Duration.ofMinutes(60));
        taskManager.addTask(task1);
        assertThrows(IllegalArgumentException.class, () -> taskManager.addTask(task2),
                "Должно быть исключение из-за пересечения");
    }

    @Test
    void shouldNotThrowExceptionWhenNoIntersection() {
        LocalDateTime dateTime = LocalDateTime.now();
        Task task1 = createTask(dateTime, Duration.ofMinutes(60));
        Task task2 = createTask(dateTime.plusHours(2), Duration.ofMinutes(60));
        taskManager.addTask(task1);
        assertDoesNotThrow(() -> taskManager.addTask(task2),
                "Не должно быть исключения при отсутствии пересечения");
    }

    @Test
    void shouldLinkSubtaskToEpic() {
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = createSubtask(epicId);
        taskManager.addSubtask(subtask);
        List<Subtask> subtasks = taskManager.getEpicSubtasks(epicId);
        assertEquals(1, subtasks.size(), "Подзадача не добавлена в эпик");
        assertEquals(subtask, subtasks.get(0), "Подзадача не совпадает");
    }

    @Test
    void shouldUpdateEpicStatusWhenSubtaskChanges() {
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = createSubtask(epicId);
        taskManager.addSubtask(subtask);
        subtask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);
        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertEquals(TaskStatus.DONE, updatedEpic.getTaskStatus(), "Статус эпика не обновился");
    }
}