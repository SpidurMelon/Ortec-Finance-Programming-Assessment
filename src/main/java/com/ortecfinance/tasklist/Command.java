package com.ortecfinance.tasklist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A Command represents a simple text command. To be used in conjunction with {@link CommandParser}
 * Supports a variable amount of parameters, all expecting a single-word argument (no spaces).
 * Supports a single "trailing parameter" which can consist of more than one word.
 * @see CommandParser
 */
public class Command {
    public static final class LackingArgumentsException extends Exception {
        public LackingArgumentsException(String rawCommandString, Command command) {
            super("The command \"%s\" has too many arguments. The correct form is \"%s\"".formatted(rawCommandString, command.getHelpString()));
        }
    }
    public static final class ExcessArgumentsException extends Exception {
        public ExcessArgumentsException(String rawCommandString, Command command) {
            super("The command \"%s\" has too many arguments. The correct form is \"%s\"".formatted(rawCommandString, command.getHelpString()));
        }
    }

    private String commandId;
    private List<String> parameters;
    private boolean trailingParameter;

    /**
     * Constructs a command designed for the command line.
     * All arguments by default can only be one word long (no spaces).
     * @param commandId The unchanging constant prefix that identifies this command.
     *                  In the command "add project Book" the commandId would be "add project"
     * @param parameters The parameters that come after the commandId.
     *                   In the command "add task Book Chapter_1"
     *                   the parameters would be ["project name", "task description"]
     * @param trailingParameter If this is set to true, the argument supplied to the
     *                          last parameter can be an arbitrary amount of words instead of just one.
     */
    public Command(String commandId, List<String> parameters, boolean trailingParameter) {
        this.commandId = commandId;
        this.parameters = parameters;
        this.trailingParameter = trailingParameter;
    }

    public Command(String commandId, List<String> parameters) {
        this(commandId, parameters, false);
    }

    public Command(String commandId) {
        this(commandId, Collections.emptyList());
    }

    /**
     * Matches a raw command string against this Command object.
     * @param rawCommand A raw command string
     * @return A list of arguments (excluding the commandId). null if rawCommand does not match the form of this Command.
     * @throws LackingArgumentsException If rawCommand matches the form of this Command but not enough arguments were provided.
     * @throws ExcessArgumentsException If rawCommand matches the form of this Command but too many arguments were provided.
     */
    public List<String> extractArgs(String rawCommand) throws LackingArgumentsException, ExcessArgumentsException {
        String strippedRawCommand = rawCommand.strip();
        if (!strippedRawCommand.startsWith(commandId)) return null;
        String rawArgs = strippedRawCommand.substring(commandId.length()).stripLeading();

        String[] splitArgs;

        if (rawArgs.isBlank()) splitArgs = new String[0];
        else if (!trailingParameter) splitArgs = rawArgs.split(" ");
        else splitArgs = rawArgs.split(" ", parameters.size());

        if (splitArgs.length < parameters.size()) throw new LackingArgumentsException(strippedRawCommand, this);
        if (splitArgs.length > parameters.size()) throw new ExcessArgumentsException(strippedRawCommand, this);

        return List.of(splitArgs);
    }

    /**
     * @return A string representing the correct form of a command string to invoke this Command.
     */
    public String getHelpString() {
        StringBuilder result = new StringBuilder();
        result.append(commandId);
        for (int i = 0; i < parameters.size(); i++) {
            String param = parameters.get(i);
            if (trailingParameter && i == parameters.size()-1) result.append(" <%s...>".formatted(param));
            else result.append(" <%s>".formatted(param));
        }
        return result.toString();
    }
}
