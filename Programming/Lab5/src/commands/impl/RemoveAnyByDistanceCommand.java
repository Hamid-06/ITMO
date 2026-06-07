package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import managers.CollectionManager;

/**
 * Команда {@code remove_any_by_distance distance} — удаляет один элемент
 * коллекции, у которого поле {@code distance} равно заданному.
 *
 * @author Hamid
 * @version 1.0
 */
public class RemoveAnyByDistanceCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public RemoveAnyByDistanceCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            return error("Использование: remove_any_by_distance <distance>");
        }
        Long distance;
        try {
            distance = Long.parseLong(ctx.getArgs()[1]);
        } catch (NumberFormatException e) {
            return error("distance должен быть целым числом.");
        }

        if (manager.removeAnyByDistance(distance)) {
            return success("Элемент с distance=" + distance + " удалён.");
        }
        return error("Элемент с distance=" + distance + " не найден.");
    }

    @Override public String getName() { return "remove_any_by_distance"; }
    @Override public String getDescription() {
        return "remove_any_by_distance <d> — удалить один элемент с заданным distance";
    }
}