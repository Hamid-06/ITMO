package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class RemoveAnyByDistanceCommand implements Command {

    private final CollectionManager manager;

    
    public RemoveAnyByDistanceCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        Long distance = request.getLongArg(); // null is valid — matches null-distance routes
        boolean removed = manager.removeAnyByDistance(distance);
        String distStr = distance != null ? String.valueOf(distance) : "null";
        if (removed) {
            return CommandResponse.ok("One route with distance=" + distStr + " removed.");
        }
        return CommandResponse.fail("No route with distance=" + distStr + " found.");
    }

    @Override
    public String getName() { return "remove_any_by_distance"; }
}