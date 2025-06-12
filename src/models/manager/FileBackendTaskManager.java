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
    private final static int CSV_ID_INDEX = 0;
    private final static int CSV_TYPE_INDEX = 1;
    private final static int CSV_NAME_INDEX = 2;
    private final static int CSV_STATUS_INDEX = 3;
    private final static int CSV_DESCRIPTION_INDEX = 4;
    private final static int CSV_EPIC_ID_INDEX = 5;
    private boolean isLoadingFlag = false;

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
        String[] list = {String.valueOf(task.getId()), task.getType().toString(), task.getTaskName(),
                task.getTaskStatus().toString(), task.getTaskDescription(), epicId};
        return String.join(",", list);
    }

    private Task taskFromString(String value) {
        String[] list = value.split(",");
        Integer id = Integer.parseInt(list[CSV_ID_INDEX]);
        String type = list[CSV_TYPE_INDEX];
        String name = list[CSV_NAME_INDEX];
        TaskStatus status = TaskStatus.valueOf(list[CSV_STATUS_INDEX]);
        String description = list[CSV_DESCRIPTION_INDEX];
        Integer subtaskEpicId = type.equals(TaskType.SUBTASK.toString()) ? Integer.parseInt(list[CSV_EPIC_ID_INDEX]) : null;
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

    public void loadFromFile(File file) {
        if (!file.exists()) {
            System.out.println("Файла не существует");
            return;
        }
        isLoadingFlag = true;
        int id = idCounter;
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
                id = Math.max(id, task.getId());
            }
            idCounter = id;
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении данных из файла " + file.getAbsolutePath());
        } finally {
            isLoadingFlag = false;
            save();
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
        if (!isLoadingFlag) {
            save();
        }
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        if (!isLoadingFlag) {
            save();
        }
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        if (!isLoadingFlag) {
            save();
        }
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
