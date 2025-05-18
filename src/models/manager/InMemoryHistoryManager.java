package models.manager;

import models.tasks.Task;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager{
    private final ArrayDeque<Task> historyDeque;
    private static final int MAX_HISTORY_ELEMENTS = 10;

    public InMemoryHistoryManager(){
        this.historyDeque = new ArrayDeque<>();
    }

    @Override
    public <T extends Task> void add(T task){
        if (historyDeque.size() == MAX_HISTORY_ELEMENTS) {
            historyDeque.removeLast();
            historyDeque.addFirst(task);
        } else {
            historyDeque.addFirst(task);
        }
    }

    @Override
    public List<Task> getHistory(){
        return new ArrayList<>(historyDeque);
    }
}
