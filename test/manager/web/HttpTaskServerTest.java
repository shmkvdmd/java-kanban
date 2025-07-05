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
import ru.common.models.tasks.status.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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
}