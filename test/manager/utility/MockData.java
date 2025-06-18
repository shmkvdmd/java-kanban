package manager.utility;

import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import ru.common.models.tasks.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

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
}
