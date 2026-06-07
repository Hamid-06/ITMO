package validation.impl;
import validation.*;
import exception.ValidationException;
import models.Route;
import validation.Validator;

/**
 * Валидатор полей класса {@link Route}.
 *
 * <p>Проверяет:</p>
 * <ul>
 *   <li>{@code name} — не null и не пустая строка</li>
 *   <li>{@code coordinates} — не null</li>
 *   <li>{@code from} — не null</li>
 *   <li>{@code distance} — если задана, должна быть &gt; 1</li>
 * </ul>
 *
 * <p>Поля {@code id} и {@code creationDate} не проверяются здесь,
 * так как генерируются автоматически в {@code CollectionManager}.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class RouteValidator implements Validator<Route> {

    /**
     * Проверяет маршрут на соответствие ограничениям полей.
     *
     * @param route маршрут для проверки
     * @throws ValidationException если нарушено одно из правил
     */
    @Override
    public void validate(Route route) throws ValidationException {
        if (route == null) {
            throw new ValidationException("route", "не может быть null");
        }

        if (route.getName() == null || route.getName().isBlank()) {
            throw new ValidationException("name", "не может быть пустым");
        }

        if (route.getCoordinates() == null) {
            throw new ValidationException("coordinates", "не может быть null");
        }

        if (route.getFrom() == null) {
            throw new ValidationException("from", "не может быть null");
        }

        if (route.getDistance() != null && route.getDistance() <= 1) {
            throw new ValidationException("distance", "должно быть > 1");
        }
    }
}