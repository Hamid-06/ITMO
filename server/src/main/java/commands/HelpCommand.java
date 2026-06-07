package commands;

import network.CommandRequest;
import network.CommandResponse;


public class HelpCommand implements Command {

    @Override
    public CommandResponse execute(CommandRequest request) {
        String text =
                "Available commands:\n" +
                        "  help                           — show this help\n" +
                        "  info                           — collection type, init date, size\n" +
                        "  show                           — print all routes sorted by distance\n" +
                        "  add                            — add a new route\n" +
                        "  update <id>                    — update route with given id\n" +
                        "  remove_by_id <id>              — remove route by id\n" +
                        "  clear                          — remove all routes\n" +
                        "  add_if_min                     — add route only if less than minimum\n" +
                        "  remove_greater                 — remove all routes greater than given\n" +
                        "  remove_lower                   — remove all routes lower than given\n" +
                        "  remove_any_by_distance <dist>  — remove one route with given distance\n" +
                        "  group_counting_by_coordinates  — count routes per unique coordinates\n" +
                        "  filter_contains_name <substr>  — show routes whose name contains substr\n" +
                        "  execute_script <file>          — run commands from a script file\n" +
                        "  exit                           — quit the client";
        return CommandResponse.ok(text);
    }

    @Override
    public String getName() { return "help"; }
}