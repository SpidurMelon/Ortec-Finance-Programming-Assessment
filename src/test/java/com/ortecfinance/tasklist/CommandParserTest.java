package com.ortecfinance.tasklist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public final class CommandParserTest {
    private static final class MockedCommandCallback {
        public List<String> args;
        public int callCount = 0;

        public void call(List<String> args) {
            this.args = args;
            callCount++;
        }
    }


    private CommandParser commandParser;

    @BeforeEach
    void constructParser() {
        commandParser = new CommandParser();
    }

    @Test
    void simpleNoParamTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("help"), callback::call);

        try {
            commandParser.parseAndExecute("help");
        } catch (Command.ExcessArgumentsException | Command.LackingArgumentsException e) {
            fail();
        }

        assertThat(callback.callCount, is(1));
        assertThat(callback.args, is(Collections.emptyList()));
    }

    @Test
    void simpleOneParamTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("add project", List.of("project name")), callback::call);
        String projectName = "Book";

        try {
            commandParser.parseAndExecute("add project %s".formatted(projectName));
        } catch (Command.ExcessArgumentsException | Command.LackingArgumentsException e) {
            fail();
        }

        assertThat(callback.callCount, is(1));
        assertThat(callback.args, is(List.of(projectName)));
    }

    @Test
    void simpleTwoParamTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("add task", List.of("project name", "task name")), callback::call);
        String projectName = "Book";
        String taskName = "Chapter1";

        try {
            commandParser.parseAndExecute("add task %s %s".formatted(projectName, taskName));
        } catch (Command.ExcessArgumentsException | Command.LackingArgumentsException e) {
            fail();
        }

        assertThat(callback.callCount, is(1));
        assertThat(callback.args, is(List.of(projectName, taskName)));
    }

    @Test
    void trailingParamTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("add task", List.of("project name", "task name"), true), callback::call);
        String projectName = "Book";
        String taskName = "Chapter 1";

        try {
            commandParser.parseAndExecute("add task %s %s".formatted(projectName, taskName));
        } catch (Command.ExcessArgumentsException | Command.LackingArgumentsException e) {
            fail();
        }

        assertThat(callback.callCount, is(1));
        assertThat(callback.args, is(List.of(projectName, taskName)));
    }

    @Test
    void noTrailingParamFailTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("add task", List.of("project name", "task name"), false), callback::call);
        String projectName = "Book";
        String taskName = "Chapter 1";

        assertThrows(Command.ExcessArgumentsException.class,
                () -> commandParser.parseAndExecute("add task %s %s".formatted(projectName, taskName)));
    }

    @Test
    void excessArgTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("help"), callback::call);

        assertThrows(Command.ExcessArgumentsException.class,
                () -> commandParser.parseAndExecute("help help"));
    }

    @Test
    void lackingArgTest() {
        MockedCommandCallback callback = new MockedCommandCallback();
        commandParser.registerCommand(new Command("add project", List.of("project name")), callback::call);

        assertThrows(Command.LackingArgumentsException.class,
                () -> commandParser.parseAndExecute("add project"));
    }

    @Test
    void notFoundTest() {
        try {
            MockedCommandCallback callback = new MockedCommandCallback();
            commandParser.registerCommand(new Command("add project", List.of("project name")), callback::call);

            boolean foundCommand = commandParser.parseAndExecute("add task");

            assertThat(foundCommand, is(false));
        } catch (Command.ExcessArgumentsException | Command.LackingArgumentsException e) {
            fail();
        }
    }
}
