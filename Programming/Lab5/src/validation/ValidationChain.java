package validation;

import exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Цепочка валидаторов (паттерн Chain of Responsibility).
 *
 * <p>Последовательно применяет все добавленные валидаторы к объекту.
 * Останавливается на первой ошибке и пробрасывает {@link ValidationException}.</p>
 *
 * <p>Пример использования:</p>
 * <pre>
 *   ValidationChain&lt;Route&gt; chain = new ValidationChain&lt;&gt;()
 *       .add(new RouteNameValidator())
 *       .add(new RouteDistanceValidator());
 *   chain.validate(route); // бросит исключение при ошибке
 * </pre>
 *
 * @param <T> тип проверяемого объекта
 * @author Hamid
 * @version 1.0
 */
public class ValidationChain<T> {

    /** Список валидаторов, применяемых по порядку. */
    private final List<Validator<T>> validators = new ArrayList<>();

    /**
     * Добавляет валидатор в конец цепочки.
     *
     * @param validator валидатор для добавления (не null)
     * @return {@code this} для fluent-цепочки вызовов
     */
    public ValidationChain<T> add(Validator<T> validator) {
        if (validator != null) validators.add(validator);
        return this;
    }

    /**
     * Последовательно применяет все валидаторы к объекту.
     *
     * <p>При первом нарушении бросает {@link ValidationException} —
     * остальные валидаторы не вызываются.</p>
     *
     * @param obj объект для проверки
     * @throws ValidationException если хотя бы один валидатор не прошёл
     */
    public void validate(T obj) throws ValidationException {
        for (Validator<T> validator : validators) {
            validator.validate(obj);
        }
    }
}