package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class TaskListEndpointTest {

    private final MockMvc mockMvc;

    @Autowired
    public TaskListEndpointTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void emptyGetProjectTest() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    @DirtiesContext
    void addProjectTest() throws Exception {
        String projectName = "Book";

        mockMvc.perform(post("/projects").content(projectName))
                .andExpect(status().isOk())
                .andExpect(content().string("Added %s".formatted(projectName)));
    }

    @Test
    @DirtiesContext
    void addAndGetProjectTest() throws Exception {
        String project1Name = "Book";
        String project2Name = "Cleaning";

        mockMvc.perform(post("/projects").content(project1Name));
        mockMvc.perform(post("/projects").content(project2Name));

        mockMvc.perform(get("/projects"))
                .andExpect(content().string("[\"%s\",\"%s\"]".formatted(project1Name, project2Name)));
    }
}
