package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import managers.CollectionManager;
import models.Route;

import java.util.List;

/**
 * Команда {@code filter_contains_name name} — выводит элементы,
 * в имени которых содержится заданная подстрока.
 *
 * @author Hamid
 * @version 1.0
 */
public class FilterContainsNameCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public FilterContainsNameCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            return error("Использование: filter_contains_name <подстрока>");
        }
        String substring = ctx.getArgs()[1];
        List<Route> found = manager.filterByName(substring);

        if (found.isEmpty()) {
            return success("Нет маршрутов с именем, содержащим \"" + substring + "\".");
        }
        StringBuilder sb = new StringBuilder();
        found.forEach(r -> sb.append(r).append("\n"));
        return success(sb.toString().trim());
    }

    @Override public String getName() { return "filter_contains_name"; }
    @Override public String getDescription() {
        return "filter_contains_name <name> — вывести элементы, имя которых содержит подстроку";
    }
}