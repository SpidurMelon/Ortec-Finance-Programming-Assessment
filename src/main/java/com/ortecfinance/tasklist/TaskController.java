package com.ortecfinance.tasklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.SequencedSet;

@RestController
@RequestMapping("/projects")
public class TaskController {

    private final TaskList taskList;

    public TaskController(TaskList taskList) {
        this.taskList = taskList;
    }

    @PostMapping
    public String addProject(@RequestBody String project) {
        taskList.addProject(project);
        return "Added %s".formatted(project);
    }

    @GetMapping
    public SequencedSet<String> getProjects() {
        return taskList.getProjects();
    }
}
