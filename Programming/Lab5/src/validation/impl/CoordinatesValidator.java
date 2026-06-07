package validation.impl;

import exception.ValidationException;
import models.Coordinates;
import validation.Validator;

/**
 * Валидатор полей класса {@link Coordinates}.
 *
 * <p>Проверяет:</p>
 * <ul>
 *   <li>{@code x} — не null и строго больше {@code -176}</li>
 *   <li>{@code y} — не null</li>
 * </ul>
 *
 * @author Hamid
 * @version 1.0
 */
public class CoordinatesValidator implements Validator<Coordinates> {

    /** Минимально допустимое значение координаты X (не включительно). */
    private static final float X_MIN = -176f;

    /**
     * Проверяет объект координат на соответствие ограничениям.
     *
     * @param coords координаты для проверки
     * @throws ValidationException если нарушено одно из правил
     */
    @Override
    public void validate(Coordinates coords) throws ValidationException {
        if (coords == null) {
            throw new ValidationException("coordinates", "не может быть null");
        }

        if (coords.getX() == null) {
            throw new ValidationException("coordinates.x", "не может быть null");
        }

        if (coords.getX() <= X_MIN) {
            throw new ValidationException("coordinates.x",
                    "должно быть > " + X_MIN + ", получено: " + coords.getX());
        }

        if (coords.getY() == null) {
            throw new ValidationException("coordinates.y", "не может быть null");
        }
    }
}