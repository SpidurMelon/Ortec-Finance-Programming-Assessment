package com.ortecfinance.tasklist;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public final class TaskListTest {

    private TaskList taskList;

    @BeforeEach
    void constructTaskList() {
        taskList = new TaskList();
    }

    @Test
    void simpleAddProject() {
        final String projectName = "Book";

        taskList.addProject(projectName);

        assertThat(taskList.getProjectNames(), contains(projectName));
        assertThat(taskList.getProjectNames(), not(contains("Exercise")));
    }

    @Test
    void simpleAddTask() {
        try {
            final String projectName = "Book";
            final String taskDescription = "Chapter 1";

            taskList.addProject(projectName);
            taskList.addTask(projectName, taskDescription);

            assertThat(taskList.getTasks(projectName), hasItem(hasProperty("description", equalTo(taskDescription))));
            assertThat(taskList.getTasks(projectName), not(hasItem(hasProperty("description", equalTo("Chapter 2")))));
        } catch (TaskList.ProjectNotFoundException e) {
            fail();
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
}
