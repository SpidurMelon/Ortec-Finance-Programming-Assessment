package com.ortecfinance.tasklist;

import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public final class TaskListTest {

    private TaskList taskList;
    private Map<String, Project> projects;
    private Map<Long, Task> tasksById;
    private TreeMap<LocalDate, List<Task>> tasksByDeadline;

    @BeforeEach
    void constructTaskList() {
        projects = new LinkedHashMap<>();
        tasksById = new LinkedHashMap<>();
        tasksByDeadline = new TreeMap<>(Comparator.nullsLast(
                (date1, date2) ->
                        date1.isEqual(date2) ? 0 : (date1.isAfter(date2) ? 1 : -1)
        ));
        taskList = new TaskList(
                projects,
                tasksById,
                tasksByDeadline
        );
    }

    @Test
    void simpleAddProject() {
        final String projectName = "Book";

        taskList.addProject(projectName);

        assertThat(taskList.getProjects(), contains(projectName));
    }

    @Test
    void simpleAddTask() {
        try {
            final String projectName = "Book";
            final String taskDescription = "Chapter 1";

            taskList.addProject(projectName);
            long taskId = taskList.addTask(projectName, taskDescription);

            assertThat(taskList.getTaskDescription(taskId), is(taskDescription));
        } catch (TaskList.ProjectNotFoundException e) {
            fail();
        } catch (TaskList.TaskNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addTaskNoProject() {
        final String projectName = "Book";
        final String taskDescription = "Chapter 1";

        assertThrows(TaskList.ProjectNotFoundException.class, () -> taskList.addTask(projectName, taskDescription));
    }

    @Test
    void simpleCheckTask() {
        try {
            final String projectName = "Book";
            final String taskDescription = "Chapter 1";

            taskList.addProject(projectName);
            long taskId = taskList.addTask(projectName, taskDescription);
            taskList.check(taskId);

            assertThat(taskList.isDone(taskId), is(true));
        } catch (TaskList.ProjectNotFoundException | TaskList.TaskNotFoundException e) {
            fail();
        }
    }

    @Test
    void complexCheckUncheckTask() {
        try {
            final String project1Name = "Book";
            final String task1Description = "Chapter 1";
            final String task2Description = "Chapter 2";
            final String project2Name = "Cleaning";
            final String task3Description = "Kitchen";
            final String task4Description = "Bathroom";

            taskList.addProject(project1Name);
            long task1Id = taskList.addTask(project1Name, task1Description);
            long task2Id = taskList.addTask(project1Name, task2Description);
            taskList.addProject(project2Name);
            long task3Id = taskList.addTask(project2Name, task3Description);
            long task4Id = taskList.addTask(project2Name, task4Description);

            taskList.check(task1Id);
            taskList.check(task2Id);
            taskList.uncheck(task2Id);
            taskList.uncheck(task3Id);
            taskList.check(task3Id);

            assertThat(taskList.isDone(task1Id), is(true));
            assertThat(taskList.isDone(task2Id), is(false));
            assertThat(taskList.isDone(task3Id), is(true));
            assertThat(taskList.isDone(task4Id), is(false));
        } catch (TaskList.ProjectNotFoundException | TaskList.TaskNotFoundException e) {
            fail();
        }
    }

    @Test
    void simpleDeadlineTest() {
        try {
            final String projectName = "Book";
            final String taskDescription = "Chapter 1";
            final LocalDate deadline = LocalDate.of(2025, 12, 25);

            taskList.addProject(projectName);
            long taskId = taskList.addTask(projectName, taskDescription);
            taskList.setDeadline(taskId, deadline);

            assertThat(tasksById.get(taskId).getDeadline(), is(deadline));
        } catch (TaskList.ProjectNotFoundException | TaskList.TaskNotFoundException e) {
            fail();
        }
    }

    @Test
    void simpleDeadlineOrderingTest() {
        try {
            final String projectName = "Book";
            final String task1Description = "Chapter 1";
            final String task2Description = "Chapter 2";
            final String task3Description = "Prologue";
            final LocalDate deadline1 = LocalDate.of(2025, 12, 25);
            final LocalDate deadline2 = LocalDate.of(2026, 12, 25);
            final LocalDate deadline3 = LocalDate.of(2024, 12, 25);

            taskList.addProject(projectName);
            long task1Id = taskList.addTask(projectName, task1Description);
            long task2Id = taskList.addTask(projectName, task2Description);
            long task3Id = taskList.addTask(projectName, task3Description);
            taskList.setDeadline(task1Id, deadline1);
            taskList.setDeadline(task2Id, deadline2);
            taskList.setDeadline(task3Id, deadline3);

            assertThat(tasksByDeadline.get(deadline1).size(), is(1));
            assertThat(tasksByDeadline.get(deadline2).size(), is(1));
            assertThat(tasksByDeadline.get(deadline3).size(), is(1));
            assertThat(tasksByDeadline.values(), containsInRelativeOrder(
                    contains(tasksById.get(task3Id)),
                    contains(tasksById.get(task1Id)),
                    contains(tasksById.get(task2Id))
                    ));
        } catch (TaskList.ProjectNotFoundException | TaskList.TaskNotFoundException e) {
            fail();
        }
    }

    @Test
    void deadlineNullOrderingTest() {
        try {
            final String projectName = "Book";
            final String task1Description = "Chapter 1";
            final String task2Description = "Chapter 2";
            final String task3Description = "Prologue";
            final LocalDate deadline1 = LocalDate.of(2025, 12, 25);
            final LocalDate deadline3 = LocalDate.of(2024, 12, 25);

            taskList.addProject(projectName);
            long task1Id = taskList.addTask(projectName, task1Description);
            long task2Id = taskList.addTask(projectName, task2Description);
            long task3Id = taskList.addTask(projectName, task3Description);
            taskList.setDeadline(task1Id, deadline1);
            taskList.setDeadline(task3Id, deadline3);

            assertThat(tasksByDeadline.get(deadline1).size(), is(1));
            assertThat(tasksByDeadline.get(null).size(), is(1));
            assertThat(tasksByDeadline.get(deadline3).size(), is(1));
            assertThat(tasksByDeadline.values(), containsInRelativeOrder(
                    contains(tasksById.get(task3Id)),
                    contains(tasksById.get(task1Id)),
                    contains(tasksById.get(task2Id))
            ));
        } catch (TaskList.ProjectNotFoundException | TaskList.TaskNotFoundException e) {
            fail();
        }
    }
}
