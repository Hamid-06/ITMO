package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class RemoveByIdCommand implements Command {

    private final CollectionManager manager;

    
    public RemoveByIdCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        if (request.getStringArg() == null || request.getStringArg().isBlank()) {
            return CommandResponse.fail("No id provided. Usage: remove_by_id <id>");
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
        boolean removed = manager.removeById(id);
        if (removed) {
            return CommandResponse.ok("Route #" + id + " removed successfully.");
        }
        return CommandResponse.fail("No route with id=" + id + " found.");
    }

    @Override
    public String getName() { return "remove_by_id"; }
}