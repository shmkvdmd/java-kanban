package models.tasks;
import status.TaskStatus;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subtasksId;

    public Epic(String taskName, String taskDescription, TaskStatus taskStatus) {
        super(taskName, taskDescription, taskStatus);
        subtasksId = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtasksId(){
        return subtasksId;
    }

    @Override
    public String toString() {
        return "Epic{taskName='" + taskName + "', taskDescription='" + taskDescription +
                "', id='" + id + "', subtaskIds=" + subtasksId.toString() +
                ", taskStatus='" + taskStatus.name() + "'}";
    }
}
