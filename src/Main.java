import models.manager.TaskManager;
import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import status.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        taskManager.addTask(new Task("task1", "description", TaskStatus.NEW));
        taskManager.addTask(new Epic("task2", "description", TaskStatus.NEW));
        taskManager.addEpic(new Epic("epic1", "description", TaskStatus.NEW));
        taskManager.addEpic(new Epic("epic2", "description", TaskStatus.NEW));
        taskManager.addSubtask(new Subtask("subtask1", "description",TaskStatus.NEW, 3));
        taskManager.addSubtask(new Subtask("subtask2", "description",TaskStatus.NEW, 4));
        System.out.println(taskManager.getTaskById(1));
        System.out.println(taskManager.getEpicById(3));
        System.out.println(taskManager.getSubtaskById(5));
        Subtask subtask = taskManager.getSubtaskById(5);
        subtask.setTaskStatus(TaskStatus.DONE);
        taskManager.updateSubtask(subtask);
        System.out.println(taskManager.getEpicById(3));
        taskManager.addSubtask(new Subtask("subtask3", "description",TaskStatus.NEW, 3));
        System.out.println(taskManager.getEpicById(3));
        taskManager.deleteEpicById(3);
        System.out.println(taskManager.getEpicById(3));
        System.out.println(taskManager.getSubtaskById(5));
    }
}
