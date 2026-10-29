package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class AddCommand implements Command {

    private final CollectionManager manager;

    
    public AddCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (request.getRouteArg() == null) {
            return CommandResponse.fail("Route data is missing.");
        }
        manager.add(request.getRouteArg());
        return CommandResponse.ok("Route added successfully.");
    }

    @Override
    public String getName() { return "add"; }
}