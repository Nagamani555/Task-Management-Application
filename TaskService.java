package com.taskmanager;

import java.util.ArrayList;

public class TaskService {
    private ArrayList<Task> tasks = new ArrayList<>();
    private int autoCounter = 1;

    // Modified to accept a manual ID from the UI
    public void addTask(int id, String title) {
        tasks.add(new Task(id, title, "PENDING"));
        if (id >= autoCounter) autoCounter = id + 1;
    }

    public ArrayList<Task> getTasks() { return tasks; }

    public void updateTask(int id, String status) {
        for (Task t : tasks) {
            if (t.id == id) {
                t.status = status;
                return;
            }
        }
    }

    public void deleteTask(int id) { tasks.removeIf(t -> t.id == id); }

    public int getTotal() { return tasks.size(); }
    
    // THIS METHOD NAME MUST MATCH THE UI CALL
    public long getActive() { 
        return tasks.stream()
                .filter(t -> t.status.equals("PENDING") || t.status.equals("IN PROGRESS"))
                .count(); 
    }
    
    public long getCompleted() { 
        return tasks.stream().filter(t -> t.status.equals("COMPLETED")).count(); 
    }

    public boolean taskExists(int id) {
        return tasks.stream().anyMatch(t -> t.id == id);
    }

    public int getNextAutoId() { return autoCounter; }
}