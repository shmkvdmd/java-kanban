package models.manager;

import models.tasks.Task;

import java.util.List;

public interface HistoryManager {
    public <T extends Task> void add(T task);

    public List<Task> getHistory();
}
