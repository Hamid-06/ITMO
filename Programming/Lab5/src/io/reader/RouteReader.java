package io.reader;

import builder.RouteBuilder;
import io.console.InputHandler;
import models.Coordinates;
import models.Location;
import models.Route;

/**
 * Читает маршрут от пользователя поле за полем.
 *
 * <p>Использует {@link InputHandler} для чтения каждого поля
 * с приглашениями и повтором при ошибке.
 * Собирает объект через {@link RouteBuilder}.</p>
 *
 * <p>Не знает о коллекции, командах или файлах —
 * только читает ввод и строит объект.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class RouteReader {

    /** Обработчик ввода для чтения примитивных полей. */
    private final InputHandler input;

    /**
     * Создаёт читалку маршрутов с заданным обработчиком ввода.
     *
     * @param input обработчик ввода (не null)
     */
    public RouteReader(InputHandler input) {
        this.input = input;
    }

    /**
     * Читает полный маршрут от пользователя.
     *
     * <p>Запрашивает поля в порядке: name, coordinates, from, to, distance.
     * Поле {@code to} опционально — пустая строка означает {@code null}.
     * Поле {@code distance} тоже опционально.</p>
     *
     * @return собранный объект {@link Route}
     * @throws IllegalArgumentException если введённые данные нарушают ограничения
     */
    public Route read() {
        input.getConsole().println("─ Новый маршрут ─");

        String name = input.readNonEmpty("Название маршрута");
        Coordinates coords = readCoordinates();
        Location from = readLocation("Точка отправления (from)", false);
        Location to   = readLocationOptional();
        Long distance = input.readLongNullableGreaterThan("Дистанция", 1L);



        return new RouteBuilder()
                .setName(name)
                .setCoordinates(coords)
                .setFrom(from)
                .setTo(to)
                .setDistance(distance)
                .build();
    }

    /**
     * Читает координаты маршрута.
     *
     * <p>Запрашивает x (строго &gt; -176) и y.</p>
     *
     * @return объект {@link Coordinates}
     */
    private Coordinates readCoordinates() {
        input.getConsole().println("  [Координаты маршрута]");
        Float x = input.readFloatGreaterThan("  x", -176f);
        Long  y = input.readLong("  y");
        return new Coordinates(x, y);
    }

    /**
     * Читает локацию (точку маршрута).
     *
     * <p>Запрашивает x (примитивный long), y (Double, не null), z (Long, не null).</p>
     *
     * @param label    заголовок блока ввода для пользователя
     * @param nullable если {@code true} — весь блок можно пропустить через Enter
     * @return объект {@link Location} или {@code null} если пропущен
     */
    private Location readLocation(String label, boolean nullable) {
        input.getConsole().println("  [" + label + "]");
        long   x = input.readPrimitiveLong("  x");
        Double y = input.readDouble("  y");
        Long   z = input.readLong("  z");
        return new Location(x, y, z);
    }

    /**
     * Читает опциональную точку прибытия (to).
     *
     * <p>Сначала спрашивает подтверждение.
     * Если пользователь отказался — возвращает {@code null}.</p>
     *
     * @return объект {@link Location} или {@code null}
     */
    private Location readLocationOptional() {
        boolean specify = input.readConfirm("Указать точку прибытия (to)?");
        if (!specify) return null;
        return readLocation("Точка прибытия (to)", false);
    }
}