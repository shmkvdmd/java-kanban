package models.manager;

import models.tasks.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import status.TaskStatus;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private TaskManager taskManager;

    @BeforeEach
    public void beforeEach(){
        taskManager = Managers.getDefault(Managers.getDefaultHistory());
    }

    @Test
    public void shouldBeFixedSize(){
        for (int i = 0; i < 15; ++i){
           int taskId = taskManager.addTask(new Task("task", "description", TaskStatus.NEW));
            taskManager.getTaskById(taskId);
        }
        HistoryManager historyManager = taskManager.getHistoryManager();
        int fixedSize = 10;
        assertEquals(fixedSize, historyManager.getHistory().size());
    }

    @Test
    public void shouldReplaceOldTasks(){
        ArrayDeque<Task> tasks = new ArrayDeque<>();
        for (int i = 0; i < 15; ++i){
            int taskId = taskManager.addTask(new Task("task", "description", TaskStatus.NEW));
            if (i >= 5){
                tasks.addFirst(taskManager.getTaskById(taskId));
            } else{
                taskManager.getTaskById(taskId);
            }
        }
        HistoryManager historyManager = taskManager.getHistoryManager();
        assertEquals(new ArrayList<>(tasks), historyManager.getHistory());
    }
}