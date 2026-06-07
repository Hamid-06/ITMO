package commands;

import commands.base.Command;
import commands.base.CommandResult;
import commands.factory.CommandRegistry;
import io.console.Console;
import io.console.InputHandler;
import io.console.OutputHandler;

/**
 * Главный цикл интерактивного режима.
 *
 * <p>Читает строки от пользователя, ищет команду в реестре и выполняет её.
 * Завершается при получении результата с флагом {@code shouldExit}.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class CommandDispatcher {

    private final CommandRegistry registry;
    private final Console console;
    private final InputHandler input;
    private final OutputHandler output;

    /**
     * @param registry реестр команд
     * @param console  консоль для ввода/вывода
     * @param input    обработчик ввода для команд с интерактивным вводом
     */
    public CommandDispatcher(CommandRegistry registry,
                             Console console,
                             InputHandler input) {
        this.registry = registry;
        this.console = console;
        this.input = input;
        this.output = new OutputHandler(console);
    }

    /**
     * Запускает главный интерактивный цикл.
     *
     * <p>Цикл завершается когда:</p>
     * <ul>
     *   <li>Команда возвращает результат с {@code shouldExit = true}</li>
     *   <li>Поток ввода закончился (EOF)</li>
     * </ul>
     */
    public void run() {
        output.print("Введите команду (или 'help' для справки):");
        while (console.hasNext()) {
            console.print("> ");
            String line = console.readLine();
            if (line == null) break;
            line = line.trim();
            if (line.isBlank()) continue;

            CommandResult result = dispatch(line);
            if (result == null) continue;

            if (result.shouldExit()) {
                output.print(result.getMessage());
                break;
            }
            if (result.isSuccess()) output.print(result.getMessage());
            else output.error(result.getMessage());
        }
    }

    /**
     * Разбирает строку и выполняет соответствующую команду.
     *
     * <p>Формат строки: {@code "имя_команды [аргумент1] [аргумент2]"}.
     * Аргументы разделяются пробелом, максимум 3 части.</p>
     *
     * @param line строка ввода от пользователя
     * @return результат выполнения или {@code null} если команда не найдена
     */
    public CommandResult dispatch(String line) {
        String[] parts = line.split("\\s+", 3);
        Command cmd = registry.get(parts[0]);
        if (cmd == null) {
            output.error("Неизвестная команда: \"" + parts[0] + "\". Введите help.");
            return null;
        }
        return cmd.execute(new CommandContext(parts, input));
    }
}