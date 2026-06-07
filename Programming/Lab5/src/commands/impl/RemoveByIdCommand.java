package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import managers.CollectionManager;

/**
 * Команда {@code remove_by_id id} — удаляет маршрут по id.
 *
 * @author Hamid
 * @version 1.0
 */
public class RemoveByIdCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public RemoveByIdCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.getArgs().length < 2) return error("Использование: remove_by_id <id>");
        try {
            int id = Integer.parseInt(ctx.getArgs()[1]);
            if (manager.removeById(id)) return success("Маршрут id=" + id + " удалён.");
            return error("Маршрут с id=" + id + " не найден.");
        } catch (NumberFormatException e) {
            return error("id должен быть целым числом.");
        }
    }

    @Override public String getName() { return "remove_by_id"; }
    @Override public String getDescription() { return "remove_by_id <id> — удалить элемент по id"; }
}