package com.ortecfinance.tasklist;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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
    private final CommandParser commandParser;

    public TaskListCLI(BufferedReader reader, PrintWriter writer, TaskList taskList) {
        this.in = reader;
        this.out = writer;
        this.taskList = taskList;
        commandParser = new CommandParser();
        initializeCommands();
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
     * Registers all commands to the {@link CommandParser}
     * Should only be called once
     */
    public void initializeCommands() {
        commandParser.registerCommand(new Command("help"), this::help);
        commandParser.registerCommand(new Command("show"), this::show);
        commandParser.registerCommand(new Command("add project", List.of("project name")), this::addProject);
        commandParser.registerCommand(new Command("add task", List.of("project name", "task description"), true), this::addTask);
        commandParser.registerCommand(new Command("check", List.of("task ID")), this::check);
        commandParser.registerCommand(new Command("uncheck", List.of("task ID")), this::uncheck);
        commandParser.registerCommand(new Command("deadline", List.of("task ID", "DD-MM-YYYY")), this::deadline);
        commandParser.registerCommand(new Command("view-by-deadline"), this::viewByDeadline);
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
        try {
            boolean foundCommand = commandParser.parseAndExecute(commandLine);
            if (!foundCommand) {
                error(commandLine);
            }
        } catch (Command.ExcessArgumentsException | Command.LackingArgumentsException e) {
            out.println(e.getMessage());
        }
    }

    /**
     * Prints helpful information about the commands available to the output stream (usually the console)
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void help(List<String> args) {
        out.println(commandParser.getHelpString());
    }

    /**
     * Prints all current projects and tasks to the output stream (usually the console)
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void show(List<String> args) {
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

    /**
     * Adds a project
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void addProject(List<String> args) {
        taskList.addProject(args.get(0));
    }

    /**
     * Adds a task
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void addTask(List<String> args) {
        try {
            taskList.addTask(args.get(0), args.get(1));
        } catch (TaskList.ProjectNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    /**
     * Marks a task as completed
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void check(List<String> args) {
        try {
            long taskId = Long.parseLong(args.get(0));
            taskList.check(taskId);
        } catch (NumberFormatException e) {
            out.println("Task ID \"%s\" is not a valid number".formatted(args.get(0)));
        } catch (TaskList.TaskNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    /**
     * Marks a task as not completed
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void uncheck(List<String> args) {
        try {
            long taskId = Long.parseLong(args.get(0));
            taskList.uncheck(taskId);
        } catch (NumberFormatException e) {
            out.println("Task ID \"%s\" is not a valid number".formatted(args.get(0)));
        } catch (TaskList.TaskNotFoundException e) {
            out.println(e.getMessage());
        }
    }

    /**
     * Sets the deadline of a task
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void deadline(List<String> args) {
        try {
            long taskId = Long.parseLong(args.get(0));
            LocalDate date = LocalDate.parse(args.get(1), DATE_FORMATTER);
            taskList.setDeadline(taskId, date);
        } catch (NumberFormatException e) {
            out.println("Task ID \"%s\" is not a valid number".formatted(args.get(0)));
        } catch (TaskList.TaskNotFoundException e) {
            out.println(e.getMessage());
        } catch (DateTimeParseException e) {
            out.println("\"%s\" is not a valid date of the form <DD-MM-YYYY>");
        }
    }

    /**
     * Views the deadline of all tasks
     * @param args A list containing the arguments after the primary command (For commands with no parameters this is empty)
     */
    private void viewByDeadline(List<String> args) {
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
     * Prints a "command not found" error to the output stream (usually the console)
     * @param command The unrecognized command
     */
    private void error(String command) {
        out.printf("I don't know what the command \"%s\" is.", command);
        out.println();
    }
}
