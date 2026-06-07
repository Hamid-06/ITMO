package commands.base;

import commands.CommandContext;

/**
 * Контракт для всех команд приложения.
 *
 * @author Hamid
 * @version 1.0
 */
public interface Command {

    /**
     * Выполняет команду.
     *
     * @param ctx контекст вызова (аргументы + InputHandler)
     * @return результат выполнения
     */
    CommandResult execute(CommandContext ctx);

    /**
     * Возвращает имя команды (ключ в реестре).
     *
     * @return имя команды, например {@code "add"}
     */
    String getName();

    /**
     * Возвращает краткое описание команды для команды help.
     *
     * @return описание
     */
    String getDescription();
}