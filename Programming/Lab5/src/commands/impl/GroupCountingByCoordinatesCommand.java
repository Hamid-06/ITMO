package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import managers.CollectionManager;

import java.util.Map;

/**
 * Команда {@code group_counting_by_coordinates} — группирует элементы
 * по полю {@code coordinates} и выводит количество в каждой группе.
 *
 * @author Hamid
 * @version 1.0
 */
public class GroupCountingByCoordinatesCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public GroupCountingByCoordinatesCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (manager.isEmpty()) return success("Коллекция пуста.");

        Map<String, Long> groups = manager.groupByCoordinates();
        StringBuilder sb = new StringBuilder("Группировка по координатам:\n");
        groups.forEach((coords, count) ->
                sb.append("  ").append(coords)
                        .append(" → ").append(count)
                        .append(" маршрут(ов)\n"));
        return success(sb.toString().trim());
    }

    @Override public String getName() { return "group_counting_by_coordinates"; }
    @Override public String getDescription() {
        return "сгруппировать элементы по полю coordinates и вывести счётчики";
    }
}