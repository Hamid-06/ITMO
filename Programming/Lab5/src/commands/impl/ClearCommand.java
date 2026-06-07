package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import managers.CollectionManager;

/**
 * Команда {@code clear} — очищает коллекцию.
 *
 * @author Hamid
 * @version 1.0
 */
public class ClearCommand extends AbstractCommand {

    /** @param manager менеджер коллекции */
    public ClearCommand(CollectionManager manager) { super(manager); }

    @Override
    public CommandResult execute(CommandContext ctx) {
        manager.clear();
        return success("Коллекция очищена.");
    }

    @Override public String getName() { return "clear"; }
    @Override public String getDescription() { return "очистить коллекцию"; }
}