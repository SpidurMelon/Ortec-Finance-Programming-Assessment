package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.*;

/**
 * A TaskList is a data structure representing a To-Do list of projects.
 * Projects are uniquely identified by their name and are further subdivided into tasks.
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

    private final Map<Long, Task> tasksById;
    private final Map<String, List<Task>> tasks;
    private final TreeMap<LocalDate, List<Task>> tasksByDeadline;
    private long lastId = 0;

    public TaskList() {
        this(new LinkedHashMap<>(), new LinkedHashMap<>());
    }

    public TaskList(Map<Long, Task> tasksById, Map<String, List<Task>> tasks) {
        this(
            tasksById,
            tasks,
            new TreeMap<>((date1, date2) ->
                    date2.isEqual(date1) ? 0 : (date2.isAfter(date1) ? 1 : -1)
            )
        );
    }

    public TaskList(Map<Long, Task> tasksById, Map<String, List<Task>> tasks, TreeMap<LocalDate, List<Task>> tasksByDeadline) {
        this.tasksById = tasksById;
        this.tasks = tasks;
        this.tasksByDeadline = tasksByDeadline;
    }

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
     * @return A set of all projects
     */
    public Set<String> getProjects() {
        return tasks.keySet();
    }

    /**
     * Gets the task ids linked to a specified project.
     * @param project The name of the project
     * @return A list containing all task ids linked to the project
     * @throws ProjectNotFoundException If the specified project does not exist in this TaskList
     * @see #getTaskDescription(long)
     * @see #check(long)
     * @see #uncheck(long)
     * @see #isDone(long)
     */
    public List<Long> getTasks(String project) throws ProjectNotFoundException {
        if (!tasks.containsKey(project)) throw new ProjectNotFoundException(project);
        return tasks.get(project)
                .stream()
                .mapToLong(Task::getId)
                .boxed()
                .toList();
    }

    /**
     * @param id The task id
     * @return The description of the specified task
     */
    public String getTaskDescription(long id) throws TaskNotFoundException {
        if (!tasksById.containsKey(id)) throw new TaskNotFoundException(id);
        return tasksById.get(id).getDescription();
    }

    /**
     * Marks a task as done (completed)
     * @param id The task id
     * @throws TaskNotFoundException If the task id is not found
     * @see #uncheck(long)
     * @see #isDone(long)
     */
    public void check(long id) throws TaskNotFoundException {
        setDone(id, true);
    }

    /**
     * Marks a task as not done (not completed)
     * @param id The task id
     * @throws TaskNotFoundException If the task id is not found
     * @see #check(long)
     * @see #isDone(long)
     */
    public void uncheck(long id) throws TaskNotFoundException {
        setDone(id, false);
    }

    /**
     * Changes the "done" state of a Task
     * @param id The task id
     * @param done The new "done" state of the Task
     * @throws TaskNotFoundException If the task id is not found
     * @see #isDone(long)
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
     * @see #check(long) 
     * @see #uncheck(long)
     */
    public boolean isDone(long id) throws TaskList.TaskNotFoundException {
        if (!tasksById.containsKey(id)) throw new TaskList.TaskNotFoundException(id);
        return tasksById.get(id).isDone();
    }

    /**
     * Sets the deadline of a specified task
     * @param id The task id
     * @param deadline The day on which this task has to be completed
     * @throws TaskList.TaskNotFoundException If the task id is not found
     */
    public void setDeadline(long id, LocalDate deadline) throws TaskNotFoundException {
        if (!tasksById.containsKey(id)) throw new TaskList.TaskNotFoundException(id);
        if (!tasksByDeadline.containsKey(deadline)) tasksByDeadline.put(deadline, new ArrayList<>());
        Task task = tasksById.get(id);

        // Remove the task from its old place in tasksByDeadline
        LocalDate oldDeadline = task.getDeadline();
        if (oldDeadline != null) {
            List<Task> oldSharedDeadlines = tasksByDeadline.get(deadline);
            oldSharedDeadlines.remove(task);
            // If the old deadline now has no tasks, remove the list altogether
            if (oldSharedDeadlines.isEmpty()) tasksByDeadline.remove(deadline);
        }

        // Add the task to its new place in tasksByDeadline
        task.setDeadline(deadline);
        tasksByDeadline.get(deadline).add(task);
    }

    /**
     * Gets the deadline of a specified task
     * @param id The task id
     * @return The day on which this task has to be completed. Or null if there is no deadline.
     * @throws TaskList.TaskNotFoundException If the task id is not found
     */
    public LocalDate getDeadline(long id) throws TaskNotFoundException {
        if (!tasksById.containsKey(id)) throw new TaskList.TaskNotFoundException(id);
        return tasksById.get(id).getDeadline();
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
