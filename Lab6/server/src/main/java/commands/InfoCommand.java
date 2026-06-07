package commands;

import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;


public class InfoCommand implements Command {

    private final CollectionManager manager;

    
    public InfoCommand(CollectionManager manager) {
        this.manager = manager;
    }

    @Override
    public CommandResponse execute(CommandRequest request) {
        return CommandResponse.ok(manager.getInfo());
    }

    @Override
    public String getName() { return "info"; }
}