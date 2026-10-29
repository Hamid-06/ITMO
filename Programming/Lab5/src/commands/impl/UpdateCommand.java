package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import io.reader.RouteReader;
import managers.CollectionManager;
import models.Route;

/**
 * Команда {@code update id} — обновляет маршрут с заданным id.
 *
 * <p>Id берётся из аргумента строки ({@code args[1]}),
 * новые поля вводятся интерактивно.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class UpdateCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public UpdateCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            return error("Использование: update <id>");
        }
        int id;
        try {
            id = Integer.parseInt(ctx.getArgs()[1]);
        } catch (NumberFormatException e) {
            return error("id должен быть целым числом.");
        }

        if (manager.findById(id).isEmpty()) {
            return error("Маршрут с id=" + id + " не найден.");
        }

        try {
            RouteReader reader = new RouteReader(ctx.getInput());
            Route newData = reader.read();
            manager.update(id, newData);
            return success("Маршрут id=" + id + " обновлён.");
        } catch (IllegalArgumentException e) {
            return error("Ошибка данных: " + e.getMessage());
        }
    }

    @Override public String getName() { return "update"; }
    @Override public String getDescription() { return "update <id> — обновить элемент по id"; }
}