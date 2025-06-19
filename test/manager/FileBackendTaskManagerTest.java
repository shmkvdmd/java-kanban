package manager;

import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.FileBackendTaskManager;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static manager.utility.MockData.*;
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
        LocalDateTime dateTime = LocalDateTime.now();
        Task task = createTask(dateTime, Duration.ofMinutes(30));
        Epic epic = createEpic();
        int epicId = taskManager.addEpic(epic);
        Subtask subtask = createSubtask(dateTime.plusHours(1), Duration.ofMinutes(30), epicId);
        taskManager.addTask(task);
        taskManager.addSubtask(subtask);
        taskManager.save();

        FileBackendTaskManager loadedManager = new FileBackendTaskManager(tempFile);
        loadedManager.loadFromFile(tempFile);
        assertEquals(1, loadedManager.getAllTasks().size(), "Задачи не загрузились");
        assertEquals(1, loadedManager.getAllEpics().size(), "Эпики не загрузились");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Подзадачи не загрузились");
        assertEquals(epic.getDuration(), loadedManager.getAllEpics().get(0).getDuration());
    }
}