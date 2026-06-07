package commands.base;

import commands.CommandContext;
import managers.CollectionManager;

/**
 * Базовая реализация команды с доступом к коллекции.
 *
 * <p>Предоставляет вспомогательные методы {@link #success} и {@link #error}
 * чтобы команды не создавали {@link CommandResult} вручную.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public abstract class AbstractCommand implements Command {

    /** Менеджер коллекции — общий для всех команд. */
    protected final CollectionManager manager;

    /**
     * @param manager менеджер коллекции
     */
    protected AbstractCommand(CollectionManager manager) {
        this.manager = manager;
    }

    /**
     * Создаёт успешный результат.
     *
     * @param message сообщение пользователю
     * @return результат
     */
    protected CommandResult success(String message) {
        return CommandResult.ok(message);
    }

    /**
     * Создаёт результат с ошибкой.
     *
     * @param message описание ошибки
     * @return результат
     */
    protected CommandResult error(String message) {
        return CommandResult.fail(message);
    }
}