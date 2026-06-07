package commands;

import network.CommandRequest;
import network.CommandResponse;


public interface Command {

    
    CommandResponse execute(CommandRequest request);

    
    String getName();
}