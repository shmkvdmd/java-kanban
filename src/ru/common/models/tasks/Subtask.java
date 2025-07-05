package ru.common.models.tasks;

import ru.common.models.tasks.status.TaskStatus;
import ru.common.models.tasks.status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final Integer epicId;

    public Subtask(String taskName, String taskDescription, TaskStatus taskStatus,
                   LocalDateTime startTime, Duration duration, int epicId) {
        super(taskName, taskDescription, taskStatus, startTime, duration);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public Integer getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{taskName='" + taskName + "', taskDescription='" + taskDescription +
                "', id='" + id + "', epicId='" + epicId +
                "', taskStatus='" + taskStatus.name() + "'}";
    }
}
