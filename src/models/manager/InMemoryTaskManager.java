package models.manager;

import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> taskMap;
    protected final HashMap<Integer, Subtask> subtaskMap;
    protected final HashMap<Integer, Epic> epicMap;
    private final HistoryManager historyManager;
    public int idCounter = 0;
    Comparator<Task> comparator = (task1, task2) -> task1.getStartTime().compareTo(task2.getStartTime());
    private final TreeSet<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        this.taskMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(comparator);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
        epicMap.values().stream().forEach((task) -> {
            task.getSubtasksId().clear();
            updateEpicStatus(task);
        });
    }

    @Override
    public void deleteEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    @Override
    public Task getTaskById(Integer id) {
        Task task = taskMap.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public int addTask(Task task) {
        if (task == null) {
            return -2;
        }
        if (task instanceof Epic || task instanceof Subtask) {
            System.out.println("Не удалось добавить задачу. Неверный тип");
            return -1;
        }
        addPrioritizedTask(task);
        task.setId(++idCounter);
        taskMap.put(idCounter, task);
        return idCounter;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic != null) {
            addPrioritizedTask(subtask);
            subtask.setId(++idCounter);
            epic.getSubtasksId().add(idCounter);
            subtaskMap.put(idCounter, subtask);
            updateEpicStatus(epic);
            updateEpicTime(epic);
        }
        return idCounter;
    }

    @Override
    public int addEpic(Epic epic) {
        epic.setId(++idCounter);
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
            prioritizedTasks.remove(taskMap.get(taskId));
            addPrioritizedTask(task);
            taskMap.put(taskId, task);
        } else {
            System.out.println("Не удалось обновить задачу. Задача не найдена");
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int subtaskId = subtask.getId();
        if (subtaskMap.containsKey(subtaskId)) {
            prioritizedTasks.remove(subtaskMap.get(subtaskId));
            addPrioritizedTask(subtask);
            subtaskMap.put(subtask.getId(), subtask);
            Epic epic = epicMap.get(subtask.getEpicId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        } else {
            System.out.println("Не удалось обновить подзадачу. Подзадача не найдена");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        int epicId = epic.getId();
        if (epicMap.containsKey(epicId)) {
            epicMap.put(epic.getId(), epic);
            updateEpicStatus(epic);
        } else {
            System.out.println("Не удалось обновить эпик. Эпик не найден");
        }
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (taskMap.containsKey(id)) {
            prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Задача не найдена");
        }
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            Epic epic = epicMap.get(subtask.getEpicId());
            epic.getSubtasksId().remove(id);
            subtaskMap.remove(id);
            historyManager.remove(id);
            updateEpicStatus(epic);
        } else {
            System.out.println("Подзадача не найдена");
        }
    }

    @Override
    public void deleteEpicById(Integer id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            epic.getSubtasksId().stream().forEach(subtaskMap::remove);
            epicMap.remove(id);
            historyManager.remove(id);
        } else {
            System.out.println("Эпик не найден");
        }
    }

    @Override
    public List<Subtask> getEpicSubtasks(Integer id) {
        if (!epicMap.containsKey(id)) {
            return Collections.emptyList();
        }
        return epicMap.get(id).getSubtasksId().stream()
                .map(subtaskMap::get)
                .toList();
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

    public void updateEpicTime(Epic epic) {
        if (!epic.getSubtasksId().isEmpty()) {
            LocalDateTime currentMinStartTime = epic.getStartTime() != null ? epic.getStartTime() : LocalDateTime.MAX;
            LocalDateTime currentMaxEndTime = epic.getEndTime() != null ? epic.getEndTime() : LocalDateTime.MIN;
            Duration duration = Duration.ofMinutes(0);
            for (Integer id : epic.getSubtasksId()) {
                Subtask subtask = subtaskMap.get(id);
                LocalDateTime subtaskStartTime = subtask.getStartTime();
                if (subtaskStartTime != null && currentMinStartTime.isAfter(subtaskStartTime)) {
                    currentMinStartTime = subtaskStartTime;
                }
                LocalDateTime subtaskEndTime = subtask.getEndTime();
                if (subtaskEndTime != null && currentMaxEndTime.isBefore(subtaskEndTime)) {
                    currentMaxEndTime = subtaskEndTime;
                }
                duration = duration.plusMinutes(subtask.getDuration() != null ? subtask.getDuration().toMinutes() : 0);
            }
            epic.setStartTime(currentMinStartTime == LocalDateTime.MAX ? null : currentMinStartTime);
            epic.setEndTime(currentMaxEndTime == LocalDateTime.MAX ? null : currentMaxEndTime);
            epic.setDuration(duration.isZero() ? null : duration);
        }
    }

    private boolean isIntersection(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getEndTime()) && task2.getStartTime().isBefore(task1.getEndTime());
    }

    private void addPrioritizedTask(Task task) {
        for (Task existingTask : prioritizedTasks) {
            if (isIntersection(task, existingTask)) {
                throw new IllegalArgumentException("Задачи пересекаются");
            }
        }
        prioritizedTasks.add(task);
    }
}
