package manager.utility;

import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import ru.common.models.tasks.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class MockData {
    public static Task createTask(String name, String description, TaskStatus status, LocalDateTime startTime,
                                  Duration duration) {
        return new Task(name, description, status, startTime, duration);
    }

    public static Task createTask() {
        return new Task("Task", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30));
    }

    public static Task createTask(LocalDateTime startTime,
                                  Duration duration) {
        return new Task("Task", "description", TaskStatus.NEW, startTime,
                duration);
    }

    public static Subtask createSubtask(String name, String description, TaskStatus status, LocalDateTime startTime,
                                        Duration duration, int epicId) {
        return new Subtask(name, description, status, startTime, duration, epicId);
    }

    public static Subtask createSubtask(int epicId) {
        return new Subtask("Subtask", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
    }

    public static Subtask createSubtask(LocalDateTime startTime,
                                        Duration duration, int epicId) {
        return new Subtask("Subtask", "description", TaskStatus.NEW, startTime, duration, epicId);
    }

    public static Subtask createSubtask(TaskStatus status, LocalDateTime dateTime, int epicId) {
        return createSubtask("Subtask", "description", status, dateTime, Duration.ofMinutes(30), epicId);
    }

    public static Epic createEpic(String name, String description, TaskStatus status) {
        return new Epic(name, description, status, LocalDateTime.now(), Duration.ZERO);
    }

    public static Epic createEpic(String name, String description, TaskStatus status, LocalDateTime startTime,
                                  Duration duration) {
        return new Epic(name, description, status, startTime, duration);
    }

    public static Epic createEpic() {
        return new Epic("Epic", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ZERO);
    }

    public static String getCreateEpicJson(String name, String description, TaskStatus status) {
        return String.format("{\"taskName\":\"%s\",\"taskDescription\":\"%s\",\"taskStatus\":\"%s\"}",
                name, description, status.name());
    }

    public static String getCreateEpicJson() {
        return getCreateEpicJson("epic", "test", TaskStatus.NEW);
    }

    public static String getCreateEpicJson(String name, String description, TaskStatus status, LocalDateTime startTime,
                                           Duration duration) {
        return String.format("{\"taskName\":\"%s\",\"taskDescription\":\"%s\",\"taskStatus\":\"%s\"," +
                        "\"startTime\":\"%s\",\"duration\":%d}",
                name, description, status.name(),
                startTime.format(ISO_LOCAL_DATE_TIME), duration.toMinutes());
    }

    public static String getCreateTaskJson(String name, String description, TaskStatus status, LocalDateTime startTime,
                                           Duration duration) {
        return String.format("{\"taskName\":\"%s\",\"taskDescription\":\"%s\",\"taskStatus\":\"%s\"," +
                        "\"startTime\":\"%s\",\"duration\":%d}",
                name, description, status.name(),
                startTime.format(ISO_LOCAL_DATE_TIME), duration.toMinutes());
    }

    public static String getCreateTaskJson() {
        return getCreateTaskJson("task", "test", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30));
    }

    public static String getCreateSubtaskJson(String name, String description, TaskStatus status, LocalDateTime startTime,
                                              Duration duration, int epicId) {
        return String.format("{\"taskName\":\"%s\",\"taskDescription\":\"%s\",\"taskStatus\":\"%s\"," +
                        "\"startTime\":\"%s\",\"duration\":%d,\"epicId\":%d}",
                name, description, status.name(),
                startTime.format(ISO_LOCAL_DATE_TIME), duration.toMinutes(), epicId);
    }

    public static String getCreateSubtaskJson(int epicId) {
        return getCreateSubtaskJson("subtask", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
    }

    public static String getUpdateTaskJson(int id, String name, String description, TaskStatus status, LocalDateTime startTime,
                                           Duration duration) {
        return String.format("{\"id\":%d,\"taskName\":\"%s\",\"taskDescription\":\"%s\",\"taskStatus\":\"%s\"," +
                        "\"startTime\":\"%s\",\"duration\":%d}",
                id, name, description, status.name(),
                startTime.format(ISO_LOCAL_DATE_TIME), duration.toMinutes());
    }

    public static String getUpdateSubtaskJson(int id, String name, String description, TaskStatus status, LocalDateTime startTime,
                                              Duration duration, int epicId) {
        return String.format("{\"id\":%d,\"taskName\":\"%s\",\"taskDescription\":\"%s\",\"taskStatus\":\"%s\"," +
                        "\"startTime\":\"%s\",\"duration\":%d,\"epicId\":%d}",
                id, name, description, status.name(),
                startTime.format(ISO_LOCAL_DATE_TIME), duration.toMinutes(), epicId);
    }

    public static String getUpdateSubtaskJson(int id, int epicId) {
        return getUpdateSubtaskJson(id, "subtaskUpdated", "description", TaskStatus.NEW, LocalDateTime.now(),
                Duration.ofMinutes(30), epicId);
    }
}
