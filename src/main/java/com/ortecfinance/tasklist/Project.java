package com.ortecfinance.tasklist;

import java.util.*;

public class Project {
    private final String name;
    private final List<Long> taskIds;

    public Project(String name) {
        this.name = name;
        this.taskIds = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void addTask(long id) {
        taskIds.add(id);
    }

    public List<Long> getTasks() {
        return Collections.unmodifiableList(taskIds);
    }
}
