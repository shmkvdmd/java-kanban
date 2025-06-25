package ru.common.manager;

import ru.common.exceptions.ManagerSaveException;
import ru.common.models.tasks.Epic;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.Task;
import ru.common.models.tasks.status.TaskStatus;
import ru.common.models.tasks.status.TaskType;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class FileBackendTaskManager extends InMemoryTaskManager {
    private final File file;
    private static final int CSV_ID_INDEX = 0;
    private static final int CSV_TYPE_INDEX = 1;
    private static final int CSV_NAME_INDEX = 2;
    private static final int CSV_STATUS_INDEX = 3;
    private static final int CSV_DESCRIPTION_INDEX = 4;
    private static final int CSV_START_TIME_INDEX = 5;
    private static final int CSV_DURATION_INDEX = 6;
    private static final int CSV_EPIC_ID_INDEX = 7;

    public FileBackendTaskManager(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Файл не создан");
        }
        this.file = file;
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            String header = "id,type,name,status,description,startTime,duration,endTime,epic\n";
            bw.write(header);
            writeToFile(bw, taskMap);
            writeToFile(bw, epicMap);
            writeToFile(bw, subtaskMap);
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения записи в файл " + file.getAbsolutePath());
        }
    }

    private void writeToFile(BufferedWriter bw, Map<Integer, ? extends Task> taskMap) throws IOException {
        for (Task task : taskMap.values()) {
            bw.write(taskToString(task) + "\n");
        }
    }

    private String taskToString(Task task) {
        String epicId = task.getType() == TaskType.SUBTASK ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "";
        String durationStr = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "0";
        String[] list = {
                String.valueOf(task.getId()),
                task.getType().toString(),
                task.getTaskName(),
                task.getTaskStatus().toString(),
                task.getTaskDescription(),
                startTimeStr,
                durationStr,
                epicId};
        return String.join(",", list);
    }

    private Task taskFromString(String value) {
        String[] list = value.split(",");
        Integer id = Integer.parseInt(list[CSV_ID_INDEX]);
        TaskType type = TaskType.valueOf(list[CSV_TYPE_INDEX]);
        String name = list[CSV_NAME_INDEX];
        TaskStatus status = TaskStatus.valueOf(list[CSV_STATUS_INDEX]);
        String description = list[CSV_DESCRIPTION_INDEX];
        LocalDateTime startTime = list[CSV_START_TIME_INDEX].isEmpty() ? null : LocalDateTime.parse(list[CSV_START_TIME_INDEX]);
        Duration duration = list[CSV_DURATION_INDEX].isEmpty() ? Duration.ZERO : Duration.ofMinutes(Long.parseLong(list[CSV_DURATION_INDEX]));
        Integer subtaskEpicId = (type == TaskType.SUBTASK) ? Integer.parseInt(list[CSV_EPIC_ID_INDEX]) : null;
        switch (type) {
            case TaskType.EPIC: {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                updateEpicTime(epic);
                return epic;
            }
            case TaskType.SUBTASK: {
                Subtask subtask = new Subtask(name, description, status, startTime, duration, subtaskEpicId);
                subtask.setId(id);
                return subtask;
            }
            case TaskType.TASK: {
                Task task = new Task(name, description, status, startTime, duration);
                task.setId(id);
                return task;
            }
            default: {
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        }
    }

    public void loadFromFile(File file) {
        if (!file.exists()) {
            System.out.println("Файла не существует");
            return;
        }
        int maxId = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (br.ready()) {
                line = br.readLine();
                if (line == null || line.isBlank()) {
                    continue;
                }
                Task task = taskFromString(line);
                int taskId = task.getId();
                maxId = Math.max(maxId, taskId);
                addTaskByType(task, taskId);
            }
            idCounter = maxId;
            save();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении данных из файла " + file.getAbsolutePath());
        }
    }

    public void addTaskByType(Task task, int taskId) {
        switch (task.getType()) {
            case TaskType.EPIC: {
                epicMap.put(taskId, (Epic) task);
                break;
            }
            case TaskType.SUBTASK: {
                Subtask subtask = (Subtask) task;
                subtaskMap.put(taskId, subtask);
                Epic epic = epicMap.get(subtask.getEpicId());
                if (epic != null) {
                    epic.getSubtasksId().add(taskId);
                    updateEpicTime(epic);
                }
                break;
            }
            case TaskType.TASK: {
                taskMap.put(taskId, task);
                break;
            }
            default: {
                throw new IllegalArgumentException("Неизвестный тип задачи: " + task.getType());
            }
        }
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTaskById(Integer id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(Integer id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(Integer id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        super.updateEpicStatus(epic);
        save();
    }
}
