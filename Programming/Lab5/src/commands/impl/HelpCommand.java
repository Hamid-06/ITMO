package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.Command;
import commands.base.CommandResult;
import commands.factory.CommandRegistry;
import managers.CollectionManager;

/**
 * Команда {@code help} — выводит список всех доступных команд с описаниями.
 *
 * @author Hamid
 * @version 1.0
 */
public class HelpCommand extends AbstractCommand {

    /** Реестр команд для получения списка. */
    private final CommandRegistry registry;

    /**
     * @param manager  менеджер коллекции
     * @param registry реестр всех зарегистрированных команд
     */
    public HelpCommand(CollectionManager manager, CommandRegistry registry) {
        super(manager);
        this.registry = registry;
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        StringBuilder sb = new StringBuilder("Доступные команды:\n");
        registry.getAll().values().stream()
                .sorted((a, b) -> a.getName().compareTo(b.getName()))
                .forEach(cmd -> sb.append(String.format("  %-40s %s%n",
                        cmd.getName(), cmd.getDescription())));
        return success(sb.toString().trim());
    }

    @Override public String getName() { return "help"; }
    @Override public String getDescription() { return "вывести справку по командам"; }
}