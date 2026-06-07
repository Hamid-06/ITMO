package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import io.reader.RouteReader;
import managers.CollectionManager;
import models.Route;

/**
 * Команда {@code remove_lower} — удаляет все элементы коллекции,
 * меньшие заданного маршрута.
 *
 * @author Hamid
 * @version 1.0
 */
public class RemoveLowerCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public RemoveLowerCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        try {
            Route route = new RouteReader(ctx.getInput()).read();
            int removed = manager.removeLower(route);
            return success("Удалено элементов: " + removed);
        } catch (IllegalArgumentException e) {
            return error("Ошибка данных: " + e.getMessage());
        }
    }

    @Override public String getName() { return "remove_lower"; }
    @Override public String getDescription() {
        return "удалить все элементы, меньшие заданного";
    }
}