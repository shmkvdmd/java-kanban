package models.tasks;

import status.TaskStatus;
import status.TaskType;

public class Subtask extends Task {
    private final int epicId;

    public Subtask(String taskName, String taskDescription, TaskStatus taskStatus, int epicId) {
        super(taskName, taskDescription, taskStatus);
        this.epicId = epicId;
    }
    @Override
    public TaskType getType(){
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
