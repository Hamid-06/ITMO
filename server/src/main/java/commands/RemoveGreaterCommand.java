package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class RemoveGreaterCommand implements Command {

    private final CollectionManager manager;

    
    public RemoveGreaterCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (request.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        int removed = manager.removeGreater(request.getRouteArg());
        return CommandResponse.ok("Removed " + removed + " route(s) greater than the given element.");
    }

    @Override
    public String getName() { return "remove_greater"; }
}