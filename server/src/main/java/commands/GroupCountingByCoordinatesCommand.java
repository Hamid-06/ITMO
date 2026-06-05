package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;

import java.util.Map;
import java.util.stream.Collectors;


public class GroupCountingByCoordinatesCommand implements Command {

    private final CollectionManager manager;

    
    public GroupCountingByCoordinatesCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (manager.isEmpty()) {
            return CommandResponse.ok("Collection is empty — nothing to group.");
        }

        Map<String, Long> groups = manager.groupByCoordinates();

        // Stream: sort groups by count descending, format each entry
        String result = groups.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> "  " + entry.getKey()
                        + "  →  " + entry.getValue() + " route(s)")
                .collect(Collectors.joining("\n"));

        return CommandResponse.ok("Grouped by coordinates:\n" + result);
    }

    @Override
    public String getName() { return "group_counting_by_coordinates"; }
}