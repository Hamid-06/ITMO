package commands.impl;

import commands.CommandContext;
import commands.base.CommandResult;
import io.reader.RouteReader;
import managers.CollectionManager;
import models.Route;

public class ShowCommand extends AddCommand {

    public ShowCommand(CollectionManager manager){
        super(manager);
    }
    @Override
    public CommandResult execute(CommandContext ctx) {
        if (manager.isEmpty()) return success("Коллекция пуста.");
        StringBuilder sb = new StringBuilder();
        manager.getAll().forEach(r -> sb.append(r).append("\n"));
        return success(sb.toString().trim());
    }

    @Override public String getName() { return "show"; }
    @Override public String getDescription() {
        return "вывести все элементы коллекции";
    }
}
