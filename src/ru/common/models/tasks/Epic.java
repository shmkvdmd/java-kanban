package ru.common.models.tasks;

import ru.common.models.tasks.status.TaskStatus;
import ru.common.models.tasks.status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId;
    private LocalDateTime endTime;

    public Epic(String taskName, String taskDescription, TaskStatus taskStatus) {
        super(taskName, taskDescription, taskStatus, LocalDateTime.now(), Duration.ZERO);
        subtasksId = new ArrayList<>();
    }

    public Epic(String taskName, String taskDescription, TaskStatus taskStatus, LocalDateTime startTime,
                Duration duration) {
        super(taskName, taskDescription, taskStatus, startTime, duration);
        subtasksId = new ArrayList<>();
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    public List<Integer> getSubtasksId() {
        return subtasksId;
    }

    @Override
    public String toString() {
        return "Epic{taskName='" + taskName + "', taskDescription='" + taskDescription +
                "', id='" + id + "', subtaskIds=" + subtasksId.toString() +
                ", taskStatus='" + taskStatus.name() + "'}";
    }
}
