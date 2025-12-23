package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

public final class TaskList {
    public static final class ProjectNotFoundException extends Exception {
        public ProjectNotFoundException(String project) {
            super("Could not find a project with the name \"%s\".".formatted(project));
        }
    }
    public static final class TaskNotFoundException extends Exception {
        public TaskNotFoundException(int id) {
            super("Could not find a task with an ID of %d.".formatted(id));
        }
    }

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    public void addProject(String name) {
        tasks.put(name, new ArrayList<Task>());
    }

    public void addTask(String project, String description) throws ProjectNotFoundException {
        if (!tasks.containsKey(project)) throw new ProjectNotFoundException(project);
        List<Task> projectTasks = tasks.get(project);
        projectTasks.add(new Task(nextId(), description, false));
    }

    public Set<Map.Entry<String, List<Task>>> getProjects() {
        return tasks.entrySet();
    }

    public Set<String> getProjectNames() {
        return tasks.keySet();
    }

    public List<Task> getTasks(String project) throws ProjectNotFoundException {
        if (!tasks.containsKey(project)) throw new ProjectNotFoundException(project);
        return tasks.get(project);
    }

    public void check(String idString) throws TaskNotFoundException {
        setDone(idString, true);
    }

    public void uncheck(String idString) throws TaskNotFoundException {
        setDone(idString, false);
    }

    private void setDone(String idString, boolean done) throws TaskNotFoundException {
        int id = Integer.parseInt(idString);
        for (Map.Entry<String, List<Task>> project : tasks.entrySet()) {
            for (Task task : project.getValue()) {
                if (task.getId() == id) {
                    task.setDone(done);
                    return;
                }
            }
        }
        throw new TaskNotFoundException(id);
    }

    private long nextId() {
        return ++lastId;
    }
}
