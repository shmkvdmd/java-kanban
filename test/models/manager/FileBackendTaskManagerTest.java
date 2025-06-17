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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackendTaskManagerTest extends TaskManagerTest<FileBackendTaskManager> {
    private File tempFile;

    @Override
    protected FileBackendTaskManager createManager() {
        try {
            tempFile = File.createTempFile("temp", "csv");
            return new FileBackendTaskManager(tempFile);
        } catch (IOException e) {
            return null;
        }
    }

    @AfterEach
    void deleteTempFile() {
        tempFile.delete();
    }

    @Test
    void shouldSaveAndLoadTasks() throws IOException {
        Task task = new Task("Task1", "Desc", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30));
        Epic epic = new Epic("Epic1", "Desc", TaskStatus.NEW);
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Sub1", "Desc", TaskStatus.NEW,
                LocalDateTime.now().plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask);
        taskManager.save();

        FileBackendTaskManager loadedManager = new FileBackendTaskManager(tempFile);
        loadedManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getAllTasks().size(), "Задачи не загрузились");
        assertEquals(1, loadedManager.getAllEpics().size(), "Эпики не загрузились");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Подзадачи не загрузились");
    }
}