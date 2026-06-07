package builder;

import models.Coordinates;
import models.Location;
import models.Route;

/**
 * Строитель объектов {@link Route} (паттерн Builder).
 *
 * <p>Разделяет конструирование маршрута от его ввода.
 * {@code RouteReader} заполняет Builder поле за полем,
 * затем вызывает {@link #build()} для получения объекта.</p>
 *
 * <p>Поля {@code id} и {@code creationDate} намеренно отсутствуют —
 * они генерируются автоматически в {@code CollectionManager}.</p>
 *
 * <p>Пример:</p>
 * <pre>
 *   Route route = new RouteBuilder()
 *       .setName("Домой")
 *       .setCoordinates(new Coordinates(10f, 20L))
 *       .setFrom(fromLocation)
 *       .setDistance(150L)
 *       .build();
 * </pre>
 *
 * @author Hamid
 * @version 1.0
 */
public class RouteBuilder {

    private String name;
    private Coordinates coordinates;
    private Location from;
    private Location to;
    private Long distance;

    /**
     * Устанавливает название маршрута.
     *
     * @param name название (не null, не пустое)
     * @return {@code this} для цепочки вызовов
     */
    public RouteBuilder setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Устанавливает координаты маршрута.
     *
     * @param coordinates координаты (не null)
     * @return {@code this} для цепочки вызовов
     */
    public RouteBuilder setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    /**
     * Устанавливает точку отправления.
     *
     * @param from точка отправления (не null)
     * @return {@code this} для цепочки вызовов
     */
    public RouteBuilder setFrom(Location from) {
        this.from = from;
        return this;
    }

    /**
     * Устанавливает точку прибытия.
     *
     * @param to точка прибытия (может быть null)
     * @return {@code this} для цепочки вызовов
     */
    public RouteBuilder setTo(Location to) {
        this.to = to;
        return this;
    }

    /**
     * Устанавливает дистанцию маршрута.
     *
     * @param distance дистанция (может быть null; если задана — должна быть &gt; 1)
     * @return {@code this} для цепочки вызовов
     */
    public RouteBuilder setDistance(Long distance) {
        this.distance = distance;
        return this;
    }

    /**
     * Строит объект {@link Route} с временным id=0 и датой = сейчас.
     *
     * <p>Настоящий id присваивается позже в {@code CollectionManager.add()}.
     * Здесь мы передаём {@code id=1} как заглушку — он будет заменён.</p>
     *
     * <p>Все проверки выполняются конструктором {@link Route} —
     * если что-то не так, будет брошен {@link IllegalArgumentException}.</p>
     *
     * @return новый объект Route
     * @throws IllegalArgumentException если обязательные поля не заполнены
     *                                  или нарушены ограничения
     */
    public Route build() {
        // id=1 — заглушка, CollectionManager заменит при add()
        // creationDate=now — тоже будет заменена в CollectionManager
        return new Route(1, name, coordinates, new java.util.Date(), from, to, distance);
    }
}