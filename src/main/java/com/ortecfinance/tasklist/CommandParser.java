package com.ortecfinance.tasklist;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A CommandParser is in charge of parsing raw command strings and matching them against various {@link Command}s.
 * Callbacks can be provided to which these arguments will be delivered.
 * @see Command
 */
public class CommandParser {
    private Map<Command, Consumer<List<String>>> registeredCommands = new LinkedHashMap<>();

    /**
     * Registers a {@link Command}.
     * If {@link #parseAndExecute} finds a match between the registered {@link Command} and a raw command string,
     * then the callback will be invoked with the extracted arguments.
     * @param command The {@link Command} to register
     * @param callback A {@link Consumer} that will receive the arguments when this command is invoked.
     * @see Command
     */
    public void registerCommand(Command command, Consumer<List<String>> callback) {
        registeredCommands.put(command, callback);
    }

    /**
     * Matches a raw command string against all registered {@link Command}s.
     * Also calls the appropriate callback function when it finds a matching {@link Command}
     * @param rawCommand The raw command string
     * @return True if the raw command string matched any of the registered {@link Command}s.
     * @throws Command.ExcessArgumentsException If a matching {@link Command} is found, but too many arguments were provided.
     * @throws Command.LackingArgumentsException If a matching {@link Command} is found, but not enough arguments were provided.
     * @see Command
     */
    public boolean parseAndExecute(String rawCommand) throws Command.ExcessArgumentsException, Command.LackingArgumentsException {
        for (Command command : registeredCommands.keySet()) {
            List<String> args = command.extractArgs(rawCommand);
            if (args != null) {
                registeredCommands.get(command).accept(args);
                return true;
            }
        }
        return false;
    }

    /**
     * @return A string showing all registered commands and how to use them.
     */
    public String getHelpString() {
        StringBuilder result = new StringBuilder();
        result.append("Commands:%n".formatted());
        for (Command command : registeredCommands.keySet()) {
            result.append("  %s%n".formatted(command.getHelpString()));
        }
        return result.toString();
    }
}
