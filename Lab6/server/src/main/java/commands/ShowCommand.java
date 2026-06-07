package commands;

import managers.CollectionManager;
import models.Route;
import network.CommandRequest;
import network.CommandResponse;

import java.util.List;


public class ShowCommand implements Command {

    private final CollectionManager manager;

    
    public ShowCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        List<Route> sorted = manager.getAllSorted();
        if (sorted.isEmpty()) {
            return CommandResponse.ok("Collection is empty.");
        }
        return CommandResponse.ok("Collection (" + sorted.size() + " element(s)):", sorted);
    }

    @Override
    public String getName() { return "show"; }
}