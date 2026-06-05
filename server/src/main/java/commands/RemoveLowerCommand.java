package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class RemoveLowerCommand implements Command {

    private final CollectionManager manager;

    
    public RemoveLowerCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (request.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        int removed = manager.removeLower(request.getRouteArg());
        return CommandResponse.ok("Removed " + removed + " route(s) lower than the given element.");
    }

    @Override
    public String getName() { return "remove_lower"; }
}