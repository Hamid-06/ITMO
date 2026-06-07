package commands.factory;

import commands.base.Command;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Реестр всех зарегистрированных команд.
 *
 * <p>Хранит отображение имя → команда. Используется
 * {@code CommandDispatcher} для поиска команды по строке ввода.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class CommandRegistry {

    /** Карта: имя команды → объект команды. */
    private final Map<String, Command> commands = new LinkedHashMap<>();

    /**
     * Регистрирует команду.
     *
     * @param name    имя команды (ключ вызова, например {@code "add"})
     * @param command объект команды
     */
    public void register(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Ищет команду по имени.
     *
     * @param name имя команды
     * @return объект {@link Command} или {@code null} если не найдена
     */
    public Command get(String name) {
        return commands.get(name);
    }

    /**
     * Возвращает все зарегистрированные команды.
     *
     * @return немодифицируемая карта команд
     */
    public Map<String, Command> getAll() {
        return Collections.unmodifiableMap(commands);
    }
}