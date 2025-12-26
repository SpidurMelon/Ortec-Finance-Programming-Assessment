package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;

/**
 * TaskListCLI is a command line interface to interact with a {@link TaskList}
 * Can read from any BufferedReader and write to any PrintWriter.
 * Uses System.in and System.out by default.
 * @see BufferedReader
 * @see PrintWriter
 * @see TaskList
 */
public class TaskListCLI implements Runnable {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String QUIT = "quit";

    private final BufferedReader in;
    private final PrintWriter out;

    private final TaskList taskList;

    public TaskListCLI(BufferedReader reader, PrintWriter writer, TaskList taskList) {
        this.in = reader;
        this.out = writer;
        this.taskList = taskList;
    }

    public TaskListCLI(BufferedReader reader, PrintWriter writer) {
        this(
                reader,
                writer,
                new TaskList()
        );
    }

    public TaskListCLI() {
        this(
                new BufferedReader(new InputStreamReader(System.in)),
                new PrintWriter(System.out)
        );
    }

    /**
     * Creates and starts a new TaskListCLI with an empty {@link TaskList}
     * @see TaskList
     */
    public static void startConsole() {
        new TaskListCLI().run();
    }

    /**
     * Starts this TaskListCLI. It will start listening to commands.
     */
    public void run() {
        out.println("Welcome to TaskList! Type 'help' for available commands.");
        while (true) {
            out.print("> ");
            out.flush();
            String command;
            try {
                command = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (command.equals(QUIT)) {
                break;
            }
            execute(command);
        }
    }

    /**
     * Executes a command
     * @param commandLine A raw user-inputted string representing a command
     */
    private void execute(String commandLine) {
        String[] commandRest = commandLine.split(" ", 2);
        String command = commandRest[0];
        switch (command) {
            case "show":
                show();
                break;
            case "add":
                add(commandRest[1]);
                break;
            case "check":
            case "uncheck":
            case "deadline":
                try {
                    String[] splitCommand = commandLine.split(" ", 3);
                    long taskId = Long.parseLong(splitCommand[1]);
                    String args = (splitCommand.length == 2 ? "" : splitCommand[2]);
                    taskCommand(command, taskId, args);
                } catch (NumberFormatException e) {
                    out.println("Task ID \"%s\" is not a valid number".formatted(commandRest[1]));
                } catch (TaskList.TaskNotFoundException e) {
                    out.println(e.getMessage());
                }
                break;
            case "view-by-deadline":
                viewByDeadline();
                break;
            case "help":
                help();
                break;
            default:
                error(command);
                break;
        }
    }

    /**
     * Executes an "add" command to add either a project or task
     * @param commandLine The arguments to a command starting with "add"
     */
    private void add(String commandLine) {
        String[] subcommandRest = commandLine.split(" ", 2);
        String subcommand = subcommandRest[0];
        if (subcommand.equals("project")) {
            taskList.addProject(subcommandRest[1]);
        } else if (subcommand.equals("task")) {
            String[] projectTask = subcommandRest[1].split(" ", 2);
            try {
                taskList.addTask(projectTask[0], projectTask[1]);
            } catch (TaskList.ProjectNotFoundException e) {
                out.println(e.getMessage());
            }
        }
    }

    /**
     * A convenience method for commands that modify tasks
     * @param command The base command (check, uncheck, deadline, etc.)
     * @param taskId The task id
     * @param args The arguments after the id
     * @throws TaskList.TaskNotFoundException If the taskList does not contain a task with the given id
     */
    private void taskCommand(String command, long taskId, String args) throws TaskList.TaskNotFoundException {
        switch (command) {
            case "check":
                taskList.check(taskId);
                break;
            case "uncheck":
                taskList.uncheck(taskId);
                break;
            case "deadline":
                try {
                    taskList.setDeadline(taskId, LocalDate.parse(args, DATE_FORMATTER));
                } catch (DateTimeParseException e) {
                    out.println(e.getMessage());
                }
                break;
        }
    }

    /**
     * Prints all current projects and tasks to the output stream (usually the console)
     */
    private void show() {
        try {
            for (String project : taskList.getProjects()) {
                out.println(project);
                for (Long taskId : taskList.getTasks(project)) {
                    out.printf("    [%c] %d: %s%n", (taskList.isDone(taskId) ? 'x' : ' '), taskId, taskList.getTaskDescription(taskId));
                }
                out.println();
            }
        } catch (TaskList.ProjectNotFoundException e) {
            throw new RuntimeException("TaskList.getProjects() listed a project that doesnt exist." +
                    "This should never happen.", e);
        } catch (TaskList.TaskNotFoundException e) {
            throw new RuntimeException("TaskList.getTasks(project) listed a task that doesnt exist." +
                    "This should never happen.", e);
        }
    }

    private void viewByDeadline() {
        try {
            SequencedMap<LocalDate, List<Long>> deadlines = taskList.getDeadlines();
            for (LocalDate deadline : deadlines.sequencedKeySet()) {
                String dateString;
                if (deadline != null) dateString = deadline.format(DATE_FORMATTER) + ":";
                else dateString = "No deadline:";
                out.println(dateString);
                for (Long taskId : deadlines.get(deadline)) {
                    out.printf("       %d: %s%n", taskId, taskList.getTaskDescription(taskId));
                }
                out.println();
            }
        } catch (TaskList.TaskNotFoundException e) {
            throw new RuntimeException("TaskList.getDeadlines() listed a task that doesnt exist." +
                    "This should never happen.", e);
        }
    }

    /**
     * Prints helpful information about the commands available to the output stream (usually the console)
     */
    private void help() {
        out.println("Commands:");
        out.println("  show");
        out.println("  add project <project name>");
        out.println("  add task <project name> <task description>");
        out.println("  check <task ID>");
        out.println("  uncheck <task ID>");
        out.println("  deadline <task ID> <DD-MM-YYYY>");
        out.println("  view-by-deadline");
        out.println();
    }

    /**
     * Prints a "command not found" error to the output stream (usually the console)
     * @param command The unrecognized command
     */
    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}
