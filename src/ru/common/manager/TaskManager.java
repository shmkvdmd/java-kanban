package ru.common.manager;

import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    HistoryManager getHistoryManager();

    List<Task> getPrioritizedTasks();

    ArrayList<Task> getAllTasks();

    ArrayList<Subtask> getAllSubtasks();

    ArrayList<Epic> getAllEpics();

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    Task getTaskById(Integer id);

    Subtask getSubtaskById(Integer id);

    Epic getEpicById(Integer id);

    int addTask(Task task);

    int addSubtask(Subtask subtask);

    int addEpic(Epic epic);

    void updateTask(Task task);

    void updateSubtask(Subtask subtask);

    void updateEpic(Epic epic);

    void deleteTaskById(Integer id);

    void deleteSubtaskById(Integer id);

    void deleteEpicById(Integer id);

    List<Subtask> getEpicSubtasks(Integer id);

    void updateEpicStatus(Epic epic);
}
