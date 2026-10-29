package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import exception.FileException;
import managers.CollectionManager;
import script.ScriptExecutor;

/**
 * Команда {@code execute_script file_name} — считывает и исполняет
 * команды из указанного файла-скрипта.
 *
 * <p>Скрипт содержит команды в том же формате, что и интерактивный режим.
 * Защита от рекурсии реализована в {@link ScriptExecutor}.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class ExecuteScriptCommand extends AbstractCommand {

    /** Исполнитель скриптов. */
    private final ScriptExecutor executor;

    /**
     * @param manager  менеджер коллекции
     * @param executor исполнитель скриптов
     */
    public ExecuteScriptCommand(CollectionManager manager, ScriptExecutor executor) {
        super(manager);
        this.executor = executor;
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        if (ctx.getArgs().length < 2) {
            return error("Использование: execute_script <имя_файла>");
        }
        String filename = ctx.getArgs()[1];
        try {
            executor.run(filename);
            return success("Скрипт \"" + filename + "\" выполнен.");
        } catch (FileException e) {
            return error("Ошибка скрипта: " + e.getMessage());
        }
    }

    @Override public String getName() { return "execute_script"; }
    @Override public String getDescription() {
        return "execute_script <file> — считать и исполнить скрипт из файла";
    }
}