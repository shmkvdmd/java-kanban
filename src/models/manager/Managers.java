package models.manager;

public class Managers {

    public static TaskManager getDefault(HistoryManager historyManager){
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistory(){
        return new InMemoryHistoryManager();
    }
}
