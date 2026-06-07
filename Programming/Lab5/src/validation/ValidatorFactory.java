package validation;

import models.Coordinates;
import models.Location;
import models.Route;
import validation.impl.CoordinatesValidator;
import validation.impl.LocationValidator;
import validation.impl.RouteValidator;

/**
 * Фабрика готовых цепочек валидаторов.
 *
 * <p>Создаёт преднастроенные {@link ValidationChain} для каждого типа объекта.
 * Используется в {@code RouteBuilder} и {@code RouteReader}.</p>
 *
 * <p>Пример:</p>
 * <pre>
 *   ValidatorFactory.forRoute().validate(route);
 *   ValidatorFactory.forCoordinates().validate(coords);
 * </pre>
 *
 * @author Hamid
 * @version 1.0
 */
public class ValidatorFactory {

    /** Приватный конструктор — только статические методы. */
    private ValidatorFactory() {}

    /**
     * Возвращает цепочку валидаторов для {@link Route}.
     *
     * @return {@link ValidationChain} для Route
     */
    public static ValidationChain<Route> forRoute() {
        return new ValidationChain<Route>()
                .add(new RouteValidator());
    }

    /**
     * Возвращает цепочку валидаторов для {@link Coordinates}.
     *
     * @return {@link ValidationChain} для Coordinates
     */
    public static ValidationChain<Coordinates> forCoordinates() {
        return new ValidationChain<Coordinates>()
                .add(new CoordinatesValidator());
    }

    /**
     * Возвращает цепочку валидаторов для {@link Location}.
     *
     * <p>Используется только для {@code from} — обязательной точки маршрута.
     * Для {@code to} валидация не нужна, так как поле nullable.</p>
     *
     * @return {@link ValidationChain} для Location
     */
    public static ValidationChain<Location> forLocation() {
        return new ValidationChain<Location>()
                .add(new LocationValidator());
    }
}