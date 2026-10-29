package io.console;

import models.Route;

import java.util.Collection;
import java.util.Map;

/**
 * Обработчик вывода результатов команд.
 *
 * <p>Форматирует и выводит результаты через {@link Console}.
 * Команды не пишут в {@code System.out} напрямую — они возвращают
 * {@code CommandResult}, а этот класс отображает его пользователю.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class OutputHandler {

    /** Консоль для вывода. */
    private final Console console;

    /**
     * Создаёт обработчик вывода с заданной консолью.
     *
     * @param console консоль для вывода
     */
    public OutputHandler(Console console) {
        this.console = console;
    }

    /**
     * Выводит обычное сообщение.
     *
     * @param message текст сообщения
     */
    public void print(String message) {
        console.println(message);
    }

    /**
     * Выводит сообщение об ошибке.
     *
     * @param message текст ошибки
     */
    public void error(String message) {
        console.error(message);
    }

    /**
     * Выводит все маршруты коллекции.
     *
     * <p>Если коллекция пуста — выводит соответствующее сообщение.</p>
     *
     * @param routes коллекция маршрутов
     */
    public void printCollection(Collection<Route> routes) {
        if (routes.isEmpty()) {
            console.println("Коллекция пуста.");
            return;
        }
        routes.forEach(r -> console.println(r.toString()));
    }

    /**
     * Выводит результат группировки по координатам.
     *
     * @param groups карта: координаты → количество маршрутов
     */
    public void printGroups(Map<String, Long> groups) {
        if (groups.isEmpty()) {
            console.println("Коллекция пуста.");
            return;
        }
        console.println("Группировка по координатам:");
        groups.forEach((coords, count) ->
                console.println("  " + coords + " → " + count + " маршрут(ов)"));
    }
}