package commands;

import network.CommandRequest;
import network.CommandResponse;


public class ExitCommand implements Command {

    @Override
    public CommandResponse execute(CommandRequest request) {
        return CommandResponse.ok("Goodbye!");
    }

    @Override
    public String getName() { return "exit"; }
}