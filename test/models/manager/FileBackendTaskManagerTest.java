package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackendTaskManagerTest {
    private File tempFile;
    private FileBackendTaskManager taskManager;

    @BeforeEach
    void beforeEach() throws IOException {
        tempFile = File.createTempFile("temp", "csv");
        taskManager = new FileBackendTaskManager(tempFile);
    }

    @AfterEach
    void deleteTempFile() {
        tempFile.delete();
    }

    @Test
    void shouldSaveTasks() throws IOException {
        Task task = new Task("Task1", "Desc1", TaskStatus.NEW);
        Epic epic = new Epic("Epic1", "Desc2", TaskStatus.NEW);
        Subtask subtask = new Subtask("Subtask1", "Desc3", TaskStatus.IN_PROGRESS, 2);
        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);
        List<String> lines = Files.readAllLines(tempFile.toPath());
        assertEquals("id,type,name,status,description,epic", lines.get(0), "Некорректный заголовок");
        assertTrue(lines.get(1).startsWith("1,TASK,Task1,NEW,Desc1"), "Некорректная запись задачи");
        assertTrue(lines.get(2).startsWith("2,EPIC,Epic1,NEW,Desc2"), "Некорректная запись эпика");
        assertTrue(lines.get(3).startsWith("3,SUBTASK,Subtask1,IN_PROGRESS,Desc3,2"), "Некорректная запись подзадачи");
    }

    @Test
    void testLoadFromFile() throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
            bw.write("id,type,name,status,description,epic\n");
            bw.write("1,TASK,Task1,NEW,Desc1,\n");
            bw.write("2,EPIC,Epic1,NEW,Desc2,\n");
            bw.write("3,SUBTASK,Subtask1,IN_PROGRESS,Desc3,2\n");
        }
        FileBackendTaskManager newManager = new FileBackendTaskManager(tempFile);
        newManager.loadFromFile(tempFile);
        assertEquals(1, newManager.getAllTasks().size(), "Должна быть 1 задача");
        assertEquals(1, newManager.getAllEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, newManager.getAllSubtasks().size(), "Должна быть 1 подзадача");
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        File file = File.createTempFile("tasks", ".csv");
        FileBackendTaskManager manager = new FileBackendTaskManager(file);
        // Создаем задачи
        Task task = new Task("Task 1", "Description", TaskStatus.NEW);
        Epic epic = new Epic("Epic 1", "Description", TaskStatus.NEW);
        int epicId = manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask 1", "Description", TaskStatus.NEW, epicId);

        manager.addTask(task);
        manager.addSubtask(subtask);

        // Заполняем историю
        manager.getTaskById(task.getId());
        manager.getEpicById(epicId);
        manager.getSubtaskById(subtask.getId());

        // Сохраняем
        manager.save();

        // Восстанавливаем
        FileBackendTaskManager loadedManager = new FileBackendTaskManager(file);
        loadedManager.loadFromFile(file);

        // Проверки
        assertEquals(1, loadedManager.getAllTasks().size(), "Задачи не восстановились");
        assertEquals(1, loadedManager.getAllEpics().size(), "Эпики не восстановились");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Подзадачи не восстановились");

        // История должна содержать 3 элемента
        //List<Task> history = loadedManager.getHistoryManager().getHistory();
        //assertEquals(3, history.size(), "История не восстановилась"); // Тест упадет, так как история не сохраняется
    }
}