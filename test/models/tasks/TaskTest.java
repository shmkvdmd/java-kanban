package models.tasks;

import models.manager.Managers;
import models.manager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    private TaskManager taskManager;
    private Task task;
    private Subtask subtask;
    private Epic epic;
    
    @BeforeEach
    public void beforeEach(){
        taskManager = Managers.getDefault();
        task = new Task("task1", "description1", TaskStatus.NEW);
        epic = new Epic("epic1", "description1", TaskStatus.NEW);
    }

    @Test
    public void isTasksEqualWithEqualId(){
        int taskId = taskManager.addTask(task);
        Task anotherTask = taskManager.getTaskById(taskId);
        assertEquals(task, anotherTask, "Экземпляры не равны");
    }

    @Test
    public void isSubtasksEqualWithEqualId(){
        taskManager.addEpic(epic);
        Subtask subtask1 = new Subtask("subtask1", "description1", TaskStatus.NEW, epic.getId());
        int subtaskId = taskManager.addSubtask(subtask1);
        Subtask subtask2 = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtask1, subtask2, "Экземпляры не равны");
    }

    @Test
    public void isEpicsEqualWithEqualId(){
        int epicId = taskManager.addEpic(epic);
        Epic anotherEpic = taskManager.getEpicById(epicId);
        assertEquals(epic, anotherEpic, "Экземпляры не равны");
    }
}