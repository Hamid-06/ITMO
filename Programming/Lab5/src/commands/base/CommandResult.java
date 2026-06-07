package commands.base;

/**
 * Результат выполнения команды.
 *
 * <p>Содержит флаг успеха, сообщение и флаг завершения программы.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class CommandResult {

    private final boolean success;
    private final String message;
    private final boolean exit;

    private CommandResult(boolean success, String message, boolean exit) {
        this.success = success;
        this.message = message;
        this.exit = exit;
    }

    /**
     * Создаёт успешный результат.
     *
     * @param message сообщение пользователю
     * @return результат
     */
    public static CommandResult ok(String message) {
        return new CommandResult(true, message, false);
    }

    /**
     * Создаёт результат с ошибкой.
     *
     * @param message описание ошибки
     * @return результат
     */
    public static CommandResult fail(String message) {
        return new CommandResult(false, message, false);
    }

    /**
     * Создаёт результат завершения программы.
     *
     * @return результат с флагом exit=true
     */
    public static CommandResult exit() {
        return new CommandResult(true, "До свидания!", true);
    }

    /** @return {@code true} если команда выполнена успешно */
    public boolean isSuccess() { return success; }

    /** @return сообщение результата */
    public String getMessage() { return message; }

    /** @return {@code true} если программа должна завершиться */
    public boolean shouldExit() { return exit; }
}