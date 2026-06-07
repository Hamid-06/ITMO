package server;

import managers.CollectionManager;
import models.Route;
import network.CommandRequest;
import network.CommandResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class RequestHandler {

    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    private final CollectionManager manager;

    
    public RequestHandler(CollectionManager manager) {
        this.manager = manager;
    }

    
    public CommandResponse handle(CommandRequest request) {
        log.fine("Handling: " + request.getType());
        try {
            return switch (request.getType()) {
                case HELP -> help();
                case INFO -> info();
                case SHOW -> show();
                case ADD -> add(request);
                case UPDATE -> update(request);
                case REMOVE_BY_ID -> removeById(request);
                case CLEAR -> clear();
                case ADD_IF_MIN -> addIfMin(request);
                case REMOVE_GREATER -> removeGreater(request);
                case REMOVE_LOWER -> removeLower(request);
                case REMOVE_ANY_BY_DISTANCE -> removeAnyByDistance(request);
                case GROUP_COUNTING_BY_COORDINATES -> groupCounting();
                case FILTER_CONTAINS_NAME -> filterContainsName(request);
                case EXECUTE_SCRIPT, EXIT -> CommandResponse.ok("OK");
            };
        } catch (Exception e) {
            log.warning("Error handling " + request.getType() + ": " + e.getMessage());
            return CommandResponse.fail("Server error: " + e.getMessage());
        }
    }

    // ── help ──────────────────────────────────────────────────────────────

    
    private CommandResponse help() {
        String text =
                "  help — show this help\n" +
                        "  info — collection type, init date, size\n" +
                        "  show — print all routes sorted by distance\n" +
                        "  add — add a new route\n" +
                        "  update <id> — update route with given id\n" +
                        "  remove_by_id <id> — remove route by id\n" +
                        "  clear — remove all routes\n" +
                        "  add_if_min — add route if less than minimum element\n" +
                        "  remove_greater — remove all routes greater than given\n" +
                        "  remove_lower — remove all routes less than given\n" +
                        "  remove_any_by_distance <dist>  — remove one route with given distance\n" +
                        "  group_counting_by_coordinates  — count routes per unique coordinates\n" +
                        "  filter_contains_name <substr>  — show routes whose name contains substr\n" +
                        "  execute_script <file> — run commands from a file\n" +
                        "  exit — quit client";
        return CommandResponse.ok(text);
    }

    // ── info ──────────────────────────────────────────────────────────────

    
    private CommandResponse info() {
        return CommandResponse.ok(manager.getInfo());
    }

    // ── show ──────────────────────────────────────────────────────────────

    
    private CommandResponse show() {
        List<Route> sorted = manager.getAllSorted();
        if (sorted.isEmpty()) {
            return CommandResponse.ok("Collection is empty.");
        }
        return CommandResponse.ok("Collection (" + sorted.size() + " element(s)):", sorted);
    }

    // ── add ───────────────────────────────────────────────────────────────

    
    private CommandResponse add(CommandRequest req) {
        if (req.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        manager.add(req.getRouteArg());
        return CommandResponse.ok("Route added successfully.");
    }

    // ── update ────────────────────────────────────────────────────────────

    
    private CommandResponse update(CommandRequest req) {
        if (req.getStringArg() == null || req.getStringArg().isBlank()) {
            return CommandResponse.fail("No id provided. Usage: update <id>");
        }
        if (req.getRouteArg() == null) {
            return CommandResponse.fail("New route data is missing.");
        }
        int id;
        try {
            id = Integer.parseInt(req.getStringArg().trim());
        } catch (NumberFormatException e) {
            return CommandResponse.fail("Id must be an integer, got: " + req.getStringArg());
        }
        if (id <= 0) {
            return CommandResponse.fail("Id must be > 0.");
        }
        boolean updated = manager.update(id, req.getRouteArg());
        if (updated) {
            return CommandResponse.ok("Route #" + id + " updated successfully.");
        }
        return CommandResponse.fail("No route with id=" + id + " found.");
    }

    // ── remove_by_id ──────────────────────────────────────────────────────

    
    private CommandResponse removeById(CommandRequest req) {
        if (req.getStringArg() == null || req.getStringArg().isBlank()) {
            return CommandResponse.fail("No id provided. Usage: remove_by_id <id>");
        }
        int id;
        try {
            id = Integer.parseInt(req.getStringArg().trim());
        } catch (NumberFormatException e) {
            return CommandResponse.fail("Id must be an integer, got: " + req.getStringArg());
        }
        if (id <= 0) {
            return CommandResponse.fail("Id must be > 0.");
        }
        boolean removed = manager.removeById(id);
        if (removed) {
            return CommandResponse.ok("Route #" + id + " removed.");
        }
        return CommandResponse.fail("No route with id=" + id + " found.");
    }

    // ── clear ─────────────────────────────────────────────────────────────

    
    private CommandResponse clear() {
        manager.clear();
        return CommandResponse.ok("Collection cleared. All routes removed.");
    }

    // ── add_if_min ────────────────────────────────────────────────────────

    
    private CommandResponse addIfMin(CommandRequest req) {
        if (req.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        int sizeBefore = manager.size();
        manager.addIfMin(req.getRouteArg());
        if (manager.size() > sizeBefore) {
            return CommandResponse.ok("Route added — it is less than the current minimum.");
        }
        return CommandResponse.ok("Route was NOT added — it is not less than the minimum.");
    }

    // ── remove_greater ────────────────────────────────────────────────────

    
    private CommandResponse removeGreater(CommandRequest req) {
        if (req.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        int removed = manager.removeGreater(req.getRouteArg());
        return CommandResponse.ok("Removed " + removed + " route(s) greater than the given element.");
    }

    // ── remove_lower ──────────────────────────────────────────────────────

    
    private CommandResponse removeLower(CommandRequest req) {
        if (req.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        int removed = manager.removeLower(req.getRouteArg());
        return CommandResponse.ok("Removed " + removed + " route(s) lower than the given element.");
    }

    // ── remove_any_by_distance ────────────────────────────────────────────

    
    private CommandResponse removeAnyByDistance(CommandRequest req) {
        Long distance = req.getLongArg();
        boolean removed = manager.removeAnyByDistance(distance);
        if (removed) {
            return CommandResponse.ok(
                    "One route with distance=" + (distance != null ? distance : "null") + " removed.");
        }
        return CommandResponse.fail(
                "No route with distance=" + (distance != null ? distance : "null") + " found.");
    }

    // ── group_counting_by_coordinates ────────────────────────────────────

    
    private CommandResponse groupCounting() {
        if (manager.isEmpty()) {
            return CommandResponse.ok("Collection is empty — nothing to group.");
        }
        Map<String, Long> groups = manager.groupByCoordinates();
        String result = groups.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> "  " + e.getKey() + "  →  " + e.getValue() + " route(s)")
                .collect(Collectors.joining("\n"));
        return CommandResponse.ok("Grouped by coordinates:\n" + result);
    }

    // ── filter_contains_name ─────────────────────────────────────────────

    
    private CommandResponse filterContainsName(CommandRequest req) {
        String substring = req.getStringArg();
        if (substring == null || substring.isBlank()) {
            return CommandResponse.fail("No substring provided. Usage: filter_contains_name <name>");
        }
        List<Route> found = manager.filterByName(substring);
        if (found.isEmpty()) {
            return CommandResponse.ok("No routes found with name containing \"" + substring + "\".");
        }
        return CommandResponse.ok(
                "Found " + found.size() + " route(s) containing \"" + substring + "\":", found);
    }
}