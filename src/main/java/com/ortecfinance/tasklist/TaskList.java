package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

/**
 * A TaskList is a data structure representing a To-Do list of projects.
 * Projects are uniquely identified by their name and are further subdivided into {@link Task}s.
 * @see Task
 */
public final class TaskList {
    public static final class ProjectNotFoundException extends Exception {
        public ProjectNotFoundException(String project) {
            super("Could not find a project with the name \"%s\".".formatted(project));
        }
    }
    public static final class TaskNotFoundException extends Exception {
        public TaskNotFoundException(long id) {
            super("Could not find a task with an ID of %d.".formatted(id));
        }
    }

    private final Map<String, List<Task>> tasks = new LinkedHashMap<>();
    private long lastId = 0;

    /**
     * Adds a project to the TaskList
     * @param name The name of the project to be added
     */
    public void addProject(String name) {
        tasks.put(name, new ArrayList<Task>());
    }

    /**
     * Adds a task to a specified project
     * @param project The project to add a task to
     * @param description The description of the task
     * @throws ProjectNotFoundException If the specified project does not exist in this TaskList
     * @see Task
     */
    public void addTask(String project, String description) throws ProjectNotFoundException {
        if (!tasks.containsKey(project)) throw new ProjectNotFoundException(project);
        List<Task> projectTasks = tasks.get(project);
        projectTasks.add(new Task(nextId(), description, false));
    }

    /**
     * @return A set of all projects, along with their tasks
     * @see Task
     */
    public Set<Map.Entry<String, List<Task>>> getProjects() {
        return tasks.entrySet();
    }


    /**
     * @return A set of all projects
     */
    public Set<String> getProjectNames() {
        return tasks.keySet();
    }

    /**
     * Gets the tasks of a specified project
     * @param project The name of the project
     * @return A list containing all tasks of the project
     * @throws ProjectNotFoundException If the specified project does not exist in this TaskList
     * @see Task
     */
    public List<Task> getTasks(String project) throws ProjectNotFoundException {
        if (!tasks.containsKey(project)) throw new ProjectNotFoundException(project);
        return tasks.get(project);
    }

    /**
     * Marks a task as done (completed)
     * @param id The task id
     * @throws TaskNotFoundException If the task id is not found
     * @see Task
     */
    public void check(long id) throws TaskNotFoundException {
        setDone(id, true);
    }

    /**
     * Marks a task as not done (not completed)
     * @param id The task id
     * @throws TaskNotFoundException If the task id is not found
     * @see Task
     */
    public void uncheck(long id) throws TaskNotFoundException {
        setDone(id, false);
    }

    /**
     * Changes the "done" state of a Task
     * @param id The task id
     * @param done The new "done" state of the Task
     * @throws TaskNotFoundException If the task id is not found
     * @see Task
     */
    private void setDone(long id, boolean done) throws TaskNotFoundException {
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

    /**
     * Gets a unique id that can be used for new Tasks.
     * Guarantees uniqueness by incrementing each time this method is called.
     * @return A monotonically increasing id
     */
    private long nextId() {
        return ++lastId;
    }
}
