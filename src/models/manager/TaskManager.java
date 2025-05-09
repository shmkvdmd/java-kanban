package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import status.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private final HashMap<Integer, Task> taskMap;
    private final HashMap<Integer, Subtask> subtaskMap;
    private final HashMap<Integer, Epic> epicMap;
    public static int idCounter = 0;

    public TaskManager() {
        this.taskMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    public void deleteTasks() {
        taskMap.clear();
    }

    public void deleteSubtasks() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtasksId().clear();
            updateEpicStatus(epic);
        }
    }

    public void deleteEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    public Task getTaskById(Integer id) {
        return taskMap.get(id);
    }

    public Subtask getSubtaskById(Integer id) {
        return subtaskMap.get(id);
    }

    public Epic getEpicById(Integer id) {
        return epicMap.get(id);
    }

    public void addTask(Task task) {
        if (task instanceof Epic || task instanceof Subtask){
            return;
        }
        ++idCounter;
        task.setId(idCounter);
        taskMap.put(idCounter, task);
    }

    public void addSubtask(Subtask subtask) {
        ++idCounter;
        subtask.setId(idCounter);
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasksId().add(idCounter);
            subtaskMap.put(idCounter, subtask);
            updateEpicStatus(epic);
        }
    }

    public void addEpic(Epic epic) {
        ++idCounter;
        epic.setId(idCounter);
        epicMap.put(idCounter, epic);
    }

    public void updateTask(Task task) {
        if (task instanceof Epic || task instanceof Subtask){
            return;
        }
        taskMap.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtaskMap.put(subtask.getId(), subtask);
        Epic epic = epicMap.get(subtask.getEpicId());
        updateEpicStatus(epic);
    }

    public void updateEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    public void deleteTaskById(Integer id) {
        taskMap.remove(id);
    }

    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            Epic epic = epicMap.get(subtask.getEpicId());
            epic.getSubtasksId().remove(id);
            subtaskMap.remove(id);
            updateEpicStatus(epic);
        }
    }

    public void deleteEpicById(Integer id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksId()) {
                subtaskMap.remove(subtaskId);
            }
            epicMap.remove(id);
        }
    }

    public List<Subtask> getEpicSubtasks(Integer id) {
        if (!epicMap.containsKey(id)) {
            return Collections.emptyList();
        }
        List<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epicMap.get(id).getSubtasksId()) {
            subtasks.add(subtaskMap.get(subtaskId));
        }
        return subtasks;
    }

    public void updateEpicStatus(Epic epic) {
        if (epic.getSubtasksId().isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else {
            boolean isSubtasksNew = true;
            boolean isSubtasksDone = true;
            for (Integer id : epic.getSubtasksId()) {
                TaskStatus subtaskStatus = subtaskMap.get(id).getTaskStatus();
                isSubtasksNew = isSubtasksNew && subtaskStatus == TaskStatus.NEW;
                isSubtasksDone = isSubtasksDone && subtaskStatus == TaskStatus.DONE;
            }
            if (isSubtasksDone) {
                epic.setTaskStatus(TaskStatus.DONE);
            } else if (isSubtasksNew) {
                epic.setTaskStatus(TaskStatus.NEW);
            } else {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
            }
        }
    }
}
