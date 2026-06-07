package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import io.reader.RouteReader;
import managers.CollectionManager;
import models.Route;

/**
 * Команда {@code add} — добавляет новый маршрут в коллекцию.
 *
 * <p>Читает поля маршрута от пользователя через {@link RouteReader}.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class AddCommand extends AbstractCommand {

    /**
     * @param manager менеджер коллекции
     */
    public AddCommand(CollectionManager manager) {
        super(manager);
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        try {
            RouteReader reader = new RouteReader(ctx.getInput());
            Route route = reader.read();
            manager.add(route);
            return success("Маршрут добавлен. id=" + manager.getAll().last().getId());
        } catch (IllegalArgumentException e) {
            return error("Ошибка данных: " + e.getMessage());
        }
    }

    @Override public String getName() { return "add"; }
    @Override public String getDescription() { return "добавить новый элемент в коллекцию"; }
}