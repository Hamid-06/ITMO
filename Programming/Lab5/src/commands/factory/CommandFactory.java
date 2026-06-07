package commands.factory;

import commands.base.Command;
import commands.impl.*;
import io.console.InputHandler;
import managers.CollectionManager;
import script.ScriptExecutor;

/**
 * Фабрика команд — создаёт все команды и внедряет зависимости.
 *
 * <p>Единственное место в программе, где команды создаются с конкретными
 * зависимостями. {@code Main} вызывает {@link #registerAll} один раз при старте.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class CommandFactory {

    private final CollectionManager manager;
    private final InputHandler input;
    private final String filename;
    private final CommandRegistry registry;
    private final ScriptExecutor scriptExecutor;

    /**
     * @param manager        менеджер коллекции
     * @param input          обработчик ввода
     * @param filename       путь к файлу (для SaveCommand)
     * @param registry       реестр команд (для HelpCommand)
     * @param scriptExecutor исполнитель скриптов (для ExecuteScriptCommand)
     */
    public CommandFactory(CollectionManager manager,
                          InputHandler input,
                          String filename,
                          CommandRegistry registry,
                          ScriptExecutor scriptExecutor) {
        this.manager = manager;
        this.input = input;
        this.filename = filename;
        this.registry = registry;
        this.scriptExecutor = scriptExecutor;
    }

    /**
     * Регистрирует все 16 команд в реестре.
     *
     * <p>Вызывается один раз из {@code Main} при инициализации.</p>
     */
    public void registerAll() {
        register(new HelpCommand(manager, registry));
        register(new InfoCommand(manager));
        register(new ShowCommand(manager));
        register(new AddCommand(manager));
        register(new UpdateCommand(manager));
        register(new RemoveByIdCommand(manager));
        register(new ClearCommand(manager));
        register(new SaveCommand(manager, filename));
        register(new ExecuteScriptCommand(manager, scriptExecutor));
        register(new ExitCommand(manager));
        register(new AddIfMinCommand(manager));
        register(new RemoveGreaterCommand(manager));
        register(new RemoveLowerCommand(manager));
        register(new RemoveAnyByDistanceCommand(manager));
        register(new GroupCountingByCoordinatesCommand(manager));
        register(new FilterContainsNameCommand(manager));
    }

    /**
     * Регистрирует одну команду в реестре по её имени.
     *
     * @param command команда для регистрации
     */
    private void register(Command command) {
        registry.register(command.getName(), command);
    }
}