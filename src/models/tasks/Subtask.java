package models.tasks;

import status.TaskStatus;
import status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String taskName, String taskDescription, TaskStatus taskStatus,
                   LocalDateTime startTime, Duration duration, int epicId) {
        super(taskName, taskDescription, taskStatus, startTime, duration);
        this.epicId = epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{taskName='" + taskName + "', taskDescription='" + taskDescription +
                "', id='" + id + "', epicId='" + epicId +
                "', taskStatus='" + taskStatus.name() + "'}";
    }
}
