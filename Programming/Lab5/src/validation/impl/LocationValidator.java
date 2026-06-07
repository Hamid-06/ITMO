package validation.impl;

import exception.ValidationException;
import models.Location;
import validation.Validator;

/**
 * Валидатор полей класса {@link Location}.
 *
 * <p>Проверяет:</p>
 * <ul>
 *   <li>{@code y} — не null</li>
 *   <li>{@code z} — не null</li>
 * </ul>
 *
 * <p>Поле {@code x} имеет примитивный тип {@code long} —
 * null для него невозможен, поэтому не проверяется.</p>
 *
 * <p>Этот валидатор применяется к {@code from}.
 * Для {@code to} — не используется, так как {@code to} может быть null целиком.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class LocationValidator implements Validator<Location> {

    /**
     * Проверяет объект локации на соответствие ограничениям.
     *
     * @param location локация для проверки
     * @throws ValidationException если нарушено одно из правил
     */
    @Override
    public void validate(Location location) throws ValidationException {
        if (location == null) {
            throw new ValidationException("location", "не может быть null");
        }

        if (location.getY() == null) {
            throw new ValidationException("location.y", "не может быть null");
        }

        if (location.getZ() == null) {
            throw new ValidationException("location.z", "не может быть null");
        }
    }
}