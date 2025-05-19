package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach(){
        taskManager = Managers.getDefault(Managers.getDefaultHistory());
    }

    @Test
    public void shouldAddTaskToManager(){
        Task task = new Task("name", "description", TaskStatus.NEW);
        taskManager.addTask(task);
        assertEquals(task, taskManager.getTaskById(task.getId()));
    }

    @Test
    public void shouldAddEpicToManager(){
        Epic epic = new Epic("name", "description", TaskStatus.NEW);
        taskManager.addEpic(epic);
        assertEquals(epic, taskManager.getEpicById(epic.getId()));
    }

    @Test
    public void shouldAddSubtaskToManager(){
        Epic epic = new Epic("name", "description", TaskStatus.NEW);
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("name", "description", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask);
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    public void shouldNotAddEpicOrSubtaskInTask(){
        Epic epic = new Epic("name", "description", TaskStatus.NEW);
        Subtask subtask = new Subtask("name", "description", TaskStatus.NEW, epic.getId());
        assertTrue(taskManager.addTask(epic) < 0);
        assertTrue(taskManager.addTask(subtask) < 0);
    }

    @Test
    public void shouldAddDifferentTasksAndFindById(){
        Task task = new Task("name", "description", TaskStatus.NEW);
        Epic epic = new Epic("name", "description", TaskStatus.NEW);
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        Subtask subtask = new Subtask("name", "description", TaskStatus.NEW, epic.getId());
        taskManager.addSubtask(subtask);
        assertEquals(task, taskManager.getTaskById(task.getId()), "Задача не найдена");
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()), "Подзадача не найдена");
        assertEquals(epic, taskManager.getEpicById(epic.getId()), "Эпик не найден");
    }

}