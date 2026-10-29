package commands;

import network.CommandRequest;
import network.CommandResponse;


public class ExecuteScriptCommand implements Command {

    @Override
    public CommandResponse execute(CommandRequest request) {
        // Script execution is client-side only.
        // Server just acknowledges the signal.
        return CommandResponse.ok("OK");
    }

    @Override
    public String getName() {
        return "execute_script";
    }
}