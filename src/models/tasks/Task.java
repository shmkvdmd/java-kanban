package models.tasks;

import status.TaskStatus;
import status.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected String taskName;
    protected String taskDescription;
    protected int id;
    protected TaskStatus taskStatus;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String taskName, String taskDescription, TaskStatus taskStatus,
                LocalDateTime startTime, Duration duration) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;

    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        Task task = (Task) obj;
        return Objects.equals(taskName, task.getTaskName())
                && Objects.equals(taskDescription, task.getTaskDescription())
                && Objects.equals(id, task.getId()) && Objects.equals(taskStatus, task.getTaskStatus());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{taskName='" + taskName + "', taskDescription='" + taskDescription +
                "', id='" + id + "', taskStatus='" + taskStatus.name() + "'}";
    }
}
