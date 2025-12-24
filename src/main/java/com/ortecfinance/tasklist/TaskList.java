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

    private final Map<Long, Task> tasksById = new LinkedHashMap<>();
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
    public long addTask(String project, String description) throws ProjectNotFoundException {
        if (!tasks.containsKey(project)) throw new ProjectNotFoundException(project);
        Task newTask = new Task(nextId(), description, false);
        List<Task> projectTasks = tasks.get(project);
        projectTasks.add(newTask);
        tasksById.put(newTask.getId(), newTask);
        return newTask.getId();
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
        if (!tasksById.containsKey(id)) throw new TaskNotFoundException(id);
        tasksById.get(id).setDone(done);
    }

    /**
     * Checks whether a certain task is marked as done
     * @param id The task id
     * @return True if the task is marked as done, false otherwise
     * @throws TaskList.TaskNotFoundException If the task id is not found
     * @see Task
     */
    public boolean isDone(long id) throws TaskList.TaskNotFoundException {
        if (!tasksById.containsKey(id)) throw new TaskList.TaskNotFoundException(id);
        return tasksById.get(id).isDone();
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
