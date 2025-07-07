package manager.web;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.common.manager.InMemoryTaskManager;
import ru.common.manager.TaskManager;
import ru.common.manager.web.HttpTaskServer;
import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import ru.common.models.tasks.status.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static manager.utility.MockData.*;

class HttpTaskServerTest {
    TaskManager manager;
    HttpTaskServer taskServer;
    Gson gson;
    HttpClient client;

    HttpTaskServerTest() throws IOException {
    }

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer(manager);
        taskServer.startServer();
        gson = taskServer.getGson();
        client = HttpClient.newHttpClient();
        // Очистка данных перед каждым тестом
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stopServer();
    }

    // Тест на создание задачи с невалидными данными
    @Test
    public void testCreateTaskWithInvalidData() throws IOException, InterruptedException {
        // JSON без обязательного поля taskName
        String invalidJson = "{\"taskDescription\":\"Test\",\"taskStatus\":\"NEW\"," +
                "\"startTime\":\"2023-01-01T00:00:00\",\"duration\":30}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Должна быть ошибка сервера");
        assertTrue(manager.getAllTasks().isEmpty(), "Задача не должна быть создана");
    }

    @Test
    public void testAddUpdateDeleteTask() throws IOException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.parse("2023-01-01T00:00:00");
        Duration duration = Duration.ofMinutes(30);
        String createJson = getCreateTaskJson("task", "Test", TaskStatus.NEW, startTime, duration);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = manager.getAllTasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        Task task = tasksFromManager.get(0);
        assertEquals("task", task.getTaskName(), "Некорректное имя задачи");
        assertEquals("Test", task.getTaskDescription(), "Некорректное описание задачи");
        assertEquals(TaskStatus.NEW, task.getTaskStatus(), "Некорректный статус задачи");
        assertEquals(startTime, task.getStartTime(), "Некорректное время начала");
        assertEquals(duration, task.getDuration(), "Некорректная длительность");

        LocalDateTime updatedStartTime = LocalDateTime.parse("2023-01-01T00:30:00");
        Duration updatedDuration = Duration.ofMinutes(90);
        String updateJson = getUpdateTaskJson(task.getId(), "taskUpdated", "updated",
                TaskStatus.IN_PROGRESS, updatedStartTime, updatedDuration);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());
        Task updatedTask = manager.getAllTasks().get(0);
        assertEquals("taskUpdated", updatedTask.getTaskName(), "Некорректное имя задачи после обновления");
        assertEquals("updated", updatedTask.getTaskDescription(), "Некорректное описание задачи после обновления");
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getTaskStatus(), "Некорректный статус задачи после обновления");
        assertEquals(updatedStartTime, updatedTask.getStartTime(), "Некорректное время начала после обновления");
        assertEquals(updatedDuration, updatedTask.getDuration(), "Некорректная длительность после обновления");

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(manager.getAllTasks().isEmpty(), "Задача не удалена");
    }

    @Test
    public void testAddDeleteEpic() throws IOException, InterruptedException {
        String createJson = getCreateEpicJson();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Epic> tasksFromManager = manager.getAllEpics();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        Epic epic = tasksFromManager.get(0);
        assertEquals("epic", epic.getTaskName(), "Некорректное имя эпика");
        assertEquals("test", epic.getTaskDescription(), "Некорректное описание эпика");
        assertEquals(TaskStatus.NEW, epic.getTaskStatus(), "Некорректный статус эпика");
        assertNotNull(epic.getStartTime(), "Время начала должно быть установлено");
        assertEquals(Duration.ZERO, epic.getDuration(), "Длительность должна быть нулевой");

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(manager.getAllEpics().isEmpty(), "Эпик не удален");
    }

    @Test
    public void testAddUpdateDeleteSubtask() throws IOException, InterruptedException {
        int epicId = manager.addEpic(createEpic());
        String createJson = getCreateSubtaskJson("Subtask", "Test", TaskStatus.NEW,
                LocalDateTime.parse("2023-01-01T00:30:00"), Duration.ofMinutes(1), epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(createJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Subtask> tasksFromManager = manager.getAllSubtasks();
        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        Subtask subtask = tasksFromManager.get(0);
        assertEquals("Subtask", subtask.getTaskName(), "Некорректное имя подзадачи");
        assertEquals("Test", subtask.getTaskDescription(), "Некорректное описание подзадачи");
        assertEquals(TaskStatus.NEW, subtask.getTaskStatus(), "Некорректный статус подзадачи");
        assertEquals(LocalDateTime.parse("2023-01-01T00:30:00"), subtask.getStartTime(), "Некорректное время начала");
        assertEquals(Duration.ofMinutes(1), subtask.getDuration(), "Некорректная длительность");
        assertEquals(epicId, subtask.getEpicId(), "Некорректный ID эпика");

        String updateJson = getUpdateSubtaskJson(subtask.getId(), "SubtaskUpdated", "TestUpdated", TaskStatus.IN_PROGRESS,
                LocalDateTime.parse("2023-01-01T00:40:00"), Duration.ofMinutes(2), epicId);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(updateJson))
                .build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());
        Subtask updatedSubtask = manager.getAllSubtasks().get(0);
        assertEquals("SubtaskUpdated", updatedSubtask.getTaskName(),
                "Некорректное имя подзадачи после обновления");
        assertEquals("TestUpdated", updatedSubtask.getTaskDescription(),
                "Некорректное описание подзадачи после обновления");
        assertEquals(TaskStatus.IN_PROGRESS, updatedSubtask.getTaskStatus(),
                "Некорректный статус подзадачи после обновления");
        assertEquals(LocalDateTime.parse("2023-01-01T00:40:00"), updatedSubtask.getStartTime(),
                "Некорректное время начала после обновления");
        assertEquals(Duration.ofMinutes(2), updatedSubtask.getDuration(), "Некорректная длительность после обновления");

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty(), "Подзадача не удалена");
    }
}