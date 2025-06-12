package models.tasks;

import status.TaskStatus;
import status.TaskType;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final ArrayList<Integer> subtasksId;

    public Epic(String taskName, String taskDescription, TaskStatus taskStatus) {
        super(taskName, taskDescription, taskStatus);
        subtasksId = new ArrayList<>();
    }

    @Override
    public TaskType getType(){
        return TaskType.EPIC;
    }

    public List<Integer> getSubtasksId() {
        return new ArrayList<>(subtasksId);
    }

    @Override
    public String toString() {
        return "Epic{taskName='" + taskName + "', taskDescription='" + taskDescription +
                "', id='" + id + "', subtaskIds=" + subtasksId.toString() +
                ", taskStatus='" + taskStatus.name() + "'}";
    }
}
