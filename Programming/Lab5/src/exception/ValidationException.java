package exception;

/**
 * Исключение, которое бросается при нарушении правил валидации.
 *
 * <p>Содержит имя поля, которое не прошло проверку,
 * и описание нарушенного правила.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class ValidationException extends Exception {

    /** Имя поля, не прошедшего валидацию. */
    private final String fieldName;

    /**
     * Создаёт исключение с указанием поля и причины.
     *
     * @param fieldName имя поля (например, {@code "distance"})
     * @param message   описание нарушения (например, {@code "должно быть > 1"})
     */
    public ValidationException(String fieldName, String message) {
        super(fieldName + ": " + message);
        this.fieldName = fieldName;
    }

    /**
     * Возвращает имя поля, не прошедшего валидацию.
     *
     * @return имя поля
     */
    public String getFieldName() {
        return fieldName;
    }
}