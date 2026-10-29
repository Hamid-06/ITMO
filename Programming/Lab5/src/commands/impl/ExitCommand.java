package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import managers.CollectionManager;

/**
 * Команда {@code exit} — завершает программу без сохранения.
 *
 * @author Hamid
 * @version 1.0
 */
public class ExitCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public ExitCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        return CommandResult.exit();
    }

    @Override public String getName() { return "exit"; }
    @Override public String getDescription() { return "завершить программу (без сохранения)"; }
}