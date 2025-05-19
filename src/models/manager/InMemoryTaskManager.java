package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import status.TaskStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayDeque;

public class InMemoryTaskManager implements TaskManager {
    private final HashMap<Integer, Task> taskMap;
    private final HashMap<Integer, Subtask> subtaskMap;
    private final HashMap<Integer, Epic> epicMap;
    private final HistoryManager historyManager;
    public static int idCounter = 0;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.taskMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.historyManager = historyManager;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epicMap.values());
    }

    @Override
    public void deleteTasks() {
        taskMap.clear();
    }

    @Override
    public void deleteSubtasks() {
        subtaskMap.clear();
        for (Epic epic : epicMap.values()) {
            epic.getSubtasksId().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = taskMap.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epicMap.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public int addTask(Task task) {
        if (task instanceof Epic || task instanceof Subtask) {
            System.out.println("Не удалось добавить задачу. Неверный тип");
            return -1;
        }
        ++idCounter;
        task.setId(idCounter);
        taskMap.put(idCounter, task);
        return idCounter;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        ++idCounter;
        subtask.setId(idCounter);
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasksId().add(idCounter);
            subtaskMap.put(idCounter, subtask);
            updateEpicStatus(epic);
        }
        return idCounter;
    }

    @Override
    public int addEpic(Epic epic) {
        ++idCounter;
        epic.setId(idCounter);
        epicMap.put(idCounter, epic);
        return idCounter;
    }

    @Override
    public void updateTask(Task task) {
        if (task instanceof Epic || task instanceof Subtask) {
            System.out.println("Не удалось обновить задачу. Неверный тип");
            return;
        }
        int taskId = task.getId();
        if (taskMap.containsKey(taskId)) {
            taskMap.put(taskId, task);
        } else {
            System.out.println("Не удалось обновить задачу. Задача не найдена");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtaskMap.put(subtask.getId(), subtask);
        Epic epic = epicMap.get(subtask.getEpicId());
        updateEpicStatus(epic);
    }

    @Override
    public void updateEpic(Epic epic) {
        epicMap.put(epic.getId(), epic);
        updateEpicStatus(epic);
    }

    @Override
    public void deleteTaskById(Integer id) {
        taskMap.remove(id);
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            Epic epic = epicMap.get(subtask.getEpicId());
            epic.getSubtasksId().remove(id);
            subtaskMap.remove(id);
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtasksId()) {
                subtaskMap.remove(subtaskId);
            }
            epicMap.remove(id);
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(Integer id) {
        if (!epicMap.containsKey(id)) {
            return Collections.emptyList();
        }
        List<Subtask> subtasks = new ArrayList<>();
        for (Integer subtaskId : epicMap.get(id).getSubtasksId()) {
            Subtask subtask = subtaskMap.get(subtaskId);
            subtasks.add(subtask);
        }
        return subtasks;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (!epic.getSubtasksId().isEmpty()) {
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
