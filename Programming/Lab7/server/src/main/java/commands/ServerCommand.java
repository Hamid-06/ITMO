package commands;

import network.CommandRequest;
import network.CommandResponse;

@FunctionalInterface
public interface ServerCommand {
    CommandResponse execute(CommandRequest request, int userId);
}
