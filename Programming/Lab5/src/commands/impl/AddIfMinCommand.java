package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import io.reader.RouteReader;
import managers.CollectionManager;
import models.Route;

public class AddIfMinCommand extends AbstractCommand {

    public AddIfMinCommand(CollectionManager  manager){
        super(manager);
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        try{
            int sizeBefore = manager.size();
            Route route = new RouteReader(ctx.getInput()).read();
            manager.addIfMin(route);
            if (sizeBefore < manager.size()) {
                return success("Маршрут добавлен — он меньше минимального.");
            }
            return success("Маршрут не добавлен — он не меньше минимального.");
        } catch (IllegalArgumentException e) {
            return error("Ошибка данных: " + e.getMessage());
        }
        }

    @Override public String getName() { return "add_if_min"; }
    @Override public String getDescription() {
        return "добавить элемент, если он меньше минимального";
    }
}
