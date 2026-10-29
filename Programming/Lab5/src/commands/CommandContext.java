package commands;

import io.console.InputHandler;

/**
 * Контекст вызова команды.
 *
 * <p>Передаёт команде аргументы строки ввода и обработчик ввода
 * для чтения составных объектов от пользователя.</p>
 *
 * <p>Пример: {@code "update 5"} → args = ["update", "5"], input = текущий InputHandler.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class CommandContext {

    /** Аргументы строки ввода: [0] — имя команды, [1..] — параметры. */
    private final String[] args;

    /** Обработчик ввода для чтения полей объектов. */
    private final InputHandler input;

    /**
     * @param args  аргументы строки ввода
     * @param input обработчик ввода
     */
    public CommandContext(String[] args, InputHandler input) {
        this.args = args;
        this.input = input;
    }

    /**
     * Возвращает аргументы строки ввода.
     *
     * @return массив аргументов
     */
    public String[] getArgs() { return args; }

    /**
     * Возвращает обработчик ввода.
     *
     * @return {@link InputHandler}
     */
    public InputHandler getInput() { return input; }
}