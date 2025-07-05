package ru.common.manager;

import ru.common.exceptions.NotFoundException;
import ru.common.exceptions.constants.ExceptionMessageConstants;
import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import ru.common.models.tasks.status.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> taskMap;
    protected final HashMap<Integer, Subtask> subtaskMap;
    protected final HashMap<Integer, Epic> epicMap;
    private final HistoryManager historyManager;
    public int idCounter = 0;
    private final Set<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        this.taskMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
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
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_TASK);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(Integer id) {
        Subtask subtask = subtaskMap.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_SUBTASK);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(Integer id) {
        Epic epic = epicMap.get(id);
        if (epic != null) {
            historyManager.add(epic);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_EPIC);
        }
        return epic;
    }

    private <T extends Task> void checkRequiredFields(T task, Consumer<T> customValidation) {
        if (task == null) {
            throw new IllegalArgumentException(ExceptionMessageConstants.NO_TASK_DATA);
        }
        if (task.getTaskName() == null || task.getTaskName().trim().isEmpty() ||
                task.getStartTime() == null || task.getDuration() == null) {
            throw new IllegalArgumentException(ExceptionMessageConstants.NO_TASK_DATA);
        }
        if (customValidation != null) {
            customValidation.accept(task);
        }
    }

    private void validateSubtask(Subtask subtask) {
        if (subtask.getEpicId() == null) {
            throw new IllegalArgumentException(ExceptionMessageConstants.NO_SUBTASK_DATA);
        }
    }

    @Override
    public int addTask(Task task) {
        checkRequiredFields(task, null);
        if (task instanceof Epic || task instanceof Subtask) {
            throw new IllegalArgumentException(ExceptionMessageConstants.ADD_WRONG_TYPE);
        }
        addPrioritizedTask(task);
        task.setId(++idCounter);
        taskMap.put(idCounter, task);
        return idCounter;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        checkRequiredFields(subtask, this::validateSubtask);
        Epic epic = epicMap.get(subtask.getEpicId());
        if (epic != null) {
            addPrioritizedTask(subtask);
            subtask.setId(++idCounter);
            epic.getSubtasksId().add(idCounter);
            subtaskMap.put(idCounter, subtask);
            updateEpicStatus(epic);
            updateEpicTime(epic);
            return idCounter;
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_EPIC);
        }
    }

    @Override
    public int addEpic(Epic epic) {
        checkRequiredFields(epic, null);
        epic.setId(++idCounter);
        epicMap.put(idCounter, epic);
        return idCounter;
    }

    @Override
    public void updateTask(Task task) {
        checkRequiredFields(task, null);
        if (task instanceof Epic || task instanceof Subtask) {
            throw new IllegalArgumentException(ExceptionMessageConstants.UPDATE_WRONG_TYPE);
        }
        int taskId = task.getId();
        if (taskMap.containsKey(taskId)) {
            prioritizedTasks.remove(taskMap.get(taskId));
            addPrioritizedTask(task);
            taskMap.put(taskId, task);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_TASK);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        checkRequiredFields(subtask, this::validateSubtask);
        int subtaskId = subtask.getId();
        if (subtaskMap.containsKey(subtaskId)) {
            prioritizedTasks.remove(subtaskMap.get(subtaskId));
            addPrioritizedTask(subtask);
            subtaskMap.put(subtask.getId(), subtask);
            Epic epic = epicMap.get(subtask.getEpicId());
            updateEpicStatus(epic);
            updateEpicTime(epic);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_SUBTASK);
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        checkRequiredFields(epic, null);
        int epicId = epic.getId();
        if (epicMap.containsKey(epicId)) {
            epicMap.put(epic.getId(), epic);
            updateEpicStatus(epic);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_EPIC);
        }
    }

    @Override
    public void deleteTaskById(Integer id) {
        if (taskMap.containsKey(id)) {
            prioritizedTasks.remove(taskMap.get(id));
            taskMap.remove(id);
            historyManager.remove(id);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_TASK);
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
            updateEpicTime(epic);
        } else {
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_SUBTASK);
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
            throw new NotFoundException(ExceptionMessageConstants.NOT_FOUND_EPIC);
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
        } else {
            epic.setStartTime(null);
            epic.setEndTime(null);
            epic.setDuration(null);
        }
    }

    private boolean isIntersection(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getEndTime()) && task2.getStartTime().isBefore(task1.getEndTime());
    }

    private void addPrioritizedTask(Task task) {
        for (Task existingTask : prioritizedTasks) {
            if (isIntersection(task, existingTask)) {
                throw new IllegalArgumentException(ExceptionMessageConstants.INTERSECTION);
            }
        }
        prioritizedTasks.add(task);
    }
}
