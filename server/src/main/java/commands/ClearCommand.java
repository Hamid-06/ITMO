package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class ClearCommand implements Command {

    private final CollectionManager manager;

    
    public ClearCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        int sizeBefore = manager.size();
        manager.clear();
        return CommandResponse.ok("Collection cleared. Removed " + sizeBefore + " route(s).");
    }

    @Override
    public String getName() { return "clear"; }
}