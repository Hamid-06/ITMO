package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class AddIfMinCommand implements Command {

    private final CollectionManager manager;

    
    public AddIfMinCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (request.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        int sizeBefore = manager.size();
        manager.addIfMin(request.getRouteArg());
        if (manager.size() > sizeBefore) {
            return CommandResponse.ok("Route added — it is less than the current minimum element.");
        }
        return CommandResponse.ok(
                "Route was NOT added — it is not less than the minimum element.");
    }

    @Override
    public String getName() { return "add_if_min"; }
}