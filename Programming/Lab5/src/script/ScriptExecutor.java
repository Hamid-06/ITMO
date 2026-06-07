package script;

import commands.CommandContext;
import commands.base.Command;
import commands.base.CommandResult;
import commands.factory.CommandRegistry;
import exception.FileException;
import io.console.Console;
import io.console.InputHandler;
import io.console.OutputHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Исполнитель скриптов — читает команды из файла и выполняет их.
 *
 * <p>Содержит защиту от рекурсии: если скрипт A вызывает скрипт A —
 * второй вызов будет отклонён с сообщением об ошибке.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class ScriptExecutor {

    /** Реестр команд для диспетчеризации строк скрипта. */
    private final CommandRegistry registry;

    /** Множество файлов-скриптов, выполняемых в данный момент (защита от рекурсии). */
    private final Set<String> runningScripts = new HashSet<>();

    /**
     * @param registry реестр всех зарегистрированных команд
     */
    public ScriptExecutor(CommandRegistry registry) {
        this.registry = registry;
    }

    /**
     * Исполняет скрипт из файла.
     *
     * <p>Каждая строка файла обрабатывается как команда.
     * Пустые строки и строки начинающиеся с {@code #} — пропускаются.</p>
     *
     * @param filename путь к файлу скрипта
     * @throws FileException если файл не найден или уже выполняется (рекурсия)
     */
    public void run(String filename) throws FileException {
        // защита от рекурсии
        if (runningScripts.contains(filename)) {
            throw new FileException("Рекурсивный вызов скрипта запрещён: " + filename);
        }

        runningScripts.add(filename);
        try (Scanner sc = new Scanner(new File(filename))) {
            Console scriptConsole = new Console(sc);
            InputHandler scriptInput = new InputHandler(scriptConsole);
            OutputHandler scriptOutput = new OutputHandler(scriptConsole);

            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isBlank() || line.startsWith("#")) continue;

                dispatch(line, scriptInput, scriptOutput);
            }
        } catch (FileNotFoundException e) {
            throw new FileException("Файл скрипта не найден: " + filename);
        } finally {
            runningScripts.remove(filename);
        }
    }

    /**
     * Разбирает строку команды и выполняет её.
     *
     * @param line   строка команды
     * @param input  обработчик ввода для команд с интерактивным вводом
     * @param output обработчик вывода результата
     */
    private void dispatch(String line, InputHandler input, OutputHandler output) {
        String[] parts = line.split("\\s+", 3);
        Command cmd = registry.get(parts[0]);
        if (cmd == null) {
            output.error("Неизвестная команда в скрипте: " + parts[0]);
            return;
        }
        CommandResult result = cmd.execute(new CommandContext(parts, input));
        if (result.isSuccess()) output.print(result.getMessage());
        else output.error(result.getMessage());
    }
}