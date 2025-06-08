package models.manager;

import exceptions.ManagerSaveException;
import models.tasks.Epic;
import models.tasks.Subtask;
import models.tasks.Task;
import status.TaskStatus;
import status.TaskType;

import java.io.*;
import java.util.Map;

public class FileBackendTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackendTaskManager(File file) {
        if (file == null) {
            throw new IllegalArgumentException("Файл не создан");
        }
        this.file = file;
    }

    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            String header = "id,type,name,status,description,epic\n";
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
        String epicId = task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "";
        String[] list = {String.valueOf(task.getId()), getTaskType(task).toString(), task.getTaskName(),
                task.getTaskStatus().toString(), task.getTaskDescription(), epicId};
        return String.join(",", list);
    }

    private Task taskFromString(String value) {
        String[] list = value.split(",");
        Integer id = Integer.parseInt(list[0]);
        String type = list[1];
        String name = list[2];
        TaskStatus status = TaskStatus.valueOf(list[3]);
        String description = list[4];
        Integer subtaskEpicId = type.equals(TaskType.SUBTASK.toString()) ? Integer.parseInt(list[5]) : null;
        switch (type) {
            case "EPIC": {
                Epic epic = new Epic(name, description, status);
                epic.setId(id);
                return epic;
            }
            case "SUBTASK": {
                Subtask subtask = new Subtask(name, description, status, subtaskEpicId);
                subtask.setId(id);
                return subtask;
            }
            case "TASK": {
                Task task = new Task(name, description, status);
                task.setId(id);
                return task;
            }
            default: {
                return null;
            }
        }
    }

    private TaskType getTaskType(Task task) {
        if (task instanceof Epic) {
            return TaskType.EPIC;
        } else if (task instanceof Subtask) {
            return TaskType.SUBTASK;
        } else {
            return TaskType.TASK;
        }
    }

    public void loadFromFile(File file) {
        if (!file.exists()) {
            System.out.println("Файла не существует");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            while (br.ready()) {
                line = br.readLine();
                if (line == null || line.isBlank()) {
                    continue;
                }
                Task task = taskFromString(line);
                if (task instanceof Epic) {
                    addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    addSubtask((Subtask) task);
                } else {
                    addTask(task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении данных из файла " + file.getAbsolutePath());
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
