package ru.common.manager;

import ru.common.manager.utility.Node;
import ru.common.models.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final HashMap<Integer, Node> historyMap;
    private Node head;
    private Node tail;

    public InMemoryHistoryManager() {
        this.historyMap = new HashMap<>();
        this.head = null;
        this.tail = null;
    }

    @Override
    public <T extends Task> void add(T task) {
        int id = task.getId();
        remove(id);
        linkLast(task);
        historyMap.put(id, tail);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (historyMap.containsKey(id)) {
            removeNode(historyMap.get(id));
            historyMap.remove(id);
        }
    }

    private void linkLast(Task task) {
        Node prev = tail;
        Node next = new Node(task, null, prev);
        tail = next;
        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
        }
    }

    private List<Task> getTasks() {
        if (head == null) {
            return new ArrayList<>();
        }
        List<Task> tasks = new ArrayList<>();
        Node tempNode = head;
        while (tempNode != null) {
            tasks.add(tempNode.task);
            tempNode = tempNode.next;
        }
        return tasks;
    }

    private void removeNode(Node node) {
        if (node == null || head == null) {
            return;
        }
        if (node.prev == null && node.next == null) {
            head = null;
            tail = null;
            return;
        }
        if (node.prev == null) {
            head = node.next;
            if (head != null) {
                head.prev = null;
            } else {
                tail = null;
            }
            return;
        }
        if (node.next == null) {
            tail = node.prev;
            tail.next = null;
            return;
        }
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

}


