package commands;

import managers.CollectionManager;
import models.Route;
import network.CommandRequest;
import network.CommandResponse;

import java.util.List;


public class FilterContainsNameCommand implements Command {

    private final CollectionManager manager;

    
    public FilterContainsNameCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        String substring = request.getStringArg();
        if (substring == null || substring.isBlank()) {
            return CommandResponse.fail(
                    "No substring provided. Usage: filter_contains_name <substring>");
        }

        List<Route> found = manager.filterByName(substring);

        if (found.isEmpty()) {
            return CommandResponse.ok(
                    "No routes found with name containing \"" + substring + "\".");
        }
        return CommandResponse.ok(
                "Found " + found.size() + " route(s) with name containing \""
                        + substring + "\":", found);
    }

    @Override
    public String getName() { return "filter_contains_name"; }
}