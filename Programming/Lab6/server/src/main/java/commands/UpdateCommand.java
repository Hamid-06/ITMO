package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class UpdateCommand implements Command {

    private final CollectionManager manager;

    
    public UpdateCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        // id comes as stringArg, new route data as routeArg
        if (request.getStringArg() == null || request.getStringArg().isBlank()) {
            return CommandResponse.fail("No id provided. Usage: update <id>");
        }
        if (request.getRouteArg() == null) {
            return CommandResponse.fail("New route data is missing.");
        }
        int id;
        try {
            id = Integer.parseInt(request.getStringArg().trim());
        } catch (NumberFormatException e) {
            return CommandResponse.fail("Id must be an integer, got: " + request.getStringArg());
        }
        if (id <= 0) {
            return CommandResponse.fail("Id must be > 0.");
        }
        boolean updated = manager.update(id, request.getRouteArg());
        if (updated) {
            return CommandResponse.ok("Route #" + id + " updated successfully.");
        }
        return CommandResponse.fail("No route with id=" + id + " found.");
    }

    @Override
    public String getName() { return "update"; }
}