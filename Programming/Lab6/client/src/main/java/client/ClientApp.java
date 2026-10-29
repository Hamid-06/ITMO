package client;

import io.Console;
import io.InputHandler;
import models.Route;
import network.CommandRequest;
import network.CommandResponse;
import network.CommandType;
import readers.RouteReader;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class ClientApp {

    private final Console         console;
    private final InputHandler    input;
    private final CommandSender   sender;
    private final ResponseHandler responseHandler;

    
    private final Set<String> runningScripts = new HashSet<>();

    
    public ClientApp(Console console, CommandSender sender) {
        this.console = console;
        this.input = new InputHandler(console);
        this.sender = sender;
        this.responseHandler = new ResponseHandler();
    }

    // --Main loop --

    
    public void run() {
        console.println("Type 'help' to see available commands.");
        while (console.hasNext()) {
            console.print("> ");
            String line = console.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.isBlank()) continue;
            boolean keepRunning = processLine(line);
            if (!keepRunning) break;
        }
        console.println("Goodbye!");
    }

    // -- Command dispatcher --

    
    public boolean processLine(String line) {
        String[] parts = line.split("\\s+", 2);
        String   cmd   = parts[0].toLowerCase();
        String   arg   = parts.length > 1 ? parts[1].trim() : "";

        switch (cmd) {

            // -- exit --
            case "exit" -> {
                return false;
            }

            // -- save (client cannot use this) --
            case "save" -> {
                console.error("'save' is a server-only command.");
                console.println("  To save, type 'save' in the SERVER terminal window.");
            }

            // -- help --
            case "help" -> sendAndPrint(new CommandRequest(CommandType.HELP));

            // ── info
            case "info" -> sendAndPrint(new CommandRequest(CommandType.INFO));

            // ── show
            case "show" -> sendAndPrint(new CommandRequest(CommandType.SHOW));

            // ── clear ──────────────────────────────────────────────────────
            case "clear" -> {
                if (input.readConfirm("Clear the entire collection?")) {
                    sendAndPrint(new CommandRequest(CommandType.CLEAR));
                } else {
                    console.println("Cancelled.");
                }
            }

            // ── add ────────────────────────────────────────────────────────
            case "add" -> {
                Route route = new RouteReader(input).read();
                sendAndPrint(new CommandRequest(CommandType.ADD, route));
            }

            // ── update <id> ────────────────────────────────────────────────
            case "update" -> {
                if (arg.isBlank()) {
                    console.error("Usage: update <id>");
                    break;
                }
                if (!isValidId(arg)) {
                    console.error("Id must be a positive integer.");
                    break;
                }
                console.println("Enter new data for route #" + arg + ":");
                Route route = new RouteReader(input).read();
                sendAndPrint(new CommandRequest(CommandType.UPDATE, arg, route));
            }

            // ── remove_by_id <id> ──────────────────────────────────────────
            case "remove_by_id" -> {
                if (arg.isBlank()) {
                    console.error("Usage: remove_by_id <id>");
                    break;
                }
                if (!isValidId(arg)) {
                    console.error("Id must be a positive integer.");
                    break;
                }
                sendAndPrint(new CommandRequest(CommandType.REMOVE_BY_ID, arg));
            }

            // ── add_if_min ─────────────────────────────────────────────────
            case "add_if_min" -> {
                console.println("Enter route to compare against the minimum:");
                Route route = new RouteReader(input).read();
                sendAndPrint(new CommandRequest(CommandType.ADD_IF_MIN, route));
            }

            // ── remove_greater ─────────────────────────────────────────────
            case "remove_greater" -> {
                console.println("Enter the reference route (routes GREATER than this will be removed):");
                Route route = new RouteReader(input).read();
                sendAndPrint(new CommandRequest(CommandType.REMOVE_GREATER, route));
            }

            // ── remove_lower ───────────────────────────────────────────────
            case "remove_lower" -> {
                console.println("Enter the reference route (routes LOWER than this will be removed):");
                Route route = new RouteReader(input).read();
                sendAndPrint(new CommandRequest(CommandType.REMOVE_LOWER, route));
            }

            // ── remove_any_by_distance <distance> ─────────────────────────
            case "remove_any_by_distance" -> {
                Long distance = parseDistanceArg(arg);
                // distance == null means user wants to match routes with null distance
                // parseDistanceArg returns null both for "null" input and invalid input;
                // we disambiguate: if arg is blank we ask interactively
                if (arg.isBlank()) {
                    distance = input.readLongNullable("Distance (Enter = match routes with null distance)");
                } else if (!arg.equalsIgnoreCase("null") && distance == null) {
                    console.error("Distance must be an integer or 'null'. Got: " + arg);
                    break;
                }
                sendAndPrint(new CommandRequest(CommandType.REMOVE_ANY_BY_DISTANCE, distance));
            }

            // ── group_counting_by_coordinates ──────────────────────────────
            case "group_counting_by_coordinates" ->
                    sendAndPrint(new CommandRequest(CommandType.GROUP_COUNTING_BY_COORDINATES));

            // ── filter_contains_name <substring> ───────────────────────────
            case "filter_contains_name" -> {
                String substring = arg.isBlank()
                        ? input.readNonEmpty("Enter name substring to search for")
                        : arg;
                sendAndPrint(new CommandRequest(CommandType.FILTER_CONTAINS_NAME, substring));
            }

            // ── execute_script <filename> ──────────────────────────────────
            case "execute_script" -> {
                String filename = arg.isBlank()
                        ? input.readNonEmpty("Script filename")
                        : arg;
                executeScript(filename);
            }

            // ── unknown ────────────────────────────────────────────────────
            default -> {
                console.error("Unknown command: '" + cmd + "'.");
                console.println("  Type 'help' to see all available commands.");
            }
        }
        return true;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    
    private void sendAndPrint(CommandRequest request) {
        CommandResponse response = sender.send(request);
        responseHandler.handle(response);
    }

    
    private void executeScript(String filename) {
        // Guard against recursion
        if (runningScripts.contains(filename)) {
            console.error("Recursive script call prevented: " + filename);
            return;
        }
        File file = new File(filename);
        if (!file.exists() || !file.isFile()) {
            console.error("Script file not found: " + filename);
            return;
        }
        if (!file.canRead()) {
            console.error("No read permission for script: " + filename);
            return;
        }

        runningScripts.add(filename);
        Scanner originalScanner = null;
        try (Scanner fileScanner = new Scanner(file)) {
            // Redirect input so RouteReader reads from the script file
            originalScanner = new Scanner(System.in);
            input.getConsole().setScanner(fileScanner);

            console.println("[script] Executing: " + filename);
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isBlank() || line.startsWith("#")) continue; // skip comments/blanks
                console.println("[script]> " + line);
                boolean keepRunning = processLine(line);
                if (!keepRunning) break;
            }
            console.println("[script] Finished: " + filename);

        } catch (Exception e) {
            console.error("Error executing script '" + filename + "': " + e.getMessage());
        } finally {
            runningScripts.remove(filename);
            // Restore stdin
            if (originalScanner != null) {
                input.getConsole().setScanner(new Scanner(System.in));
            }
        }
    }

    
    private boolean isValidId(String s) {
        try {
            return Integer.parseInt(s.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    
    private Long parseDistanceArg(String arg) {
        if (arg.isBlank() || arg.equalsIgnoreCase("null")) return null;
        try {
            return Long.parseLong(arg.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}