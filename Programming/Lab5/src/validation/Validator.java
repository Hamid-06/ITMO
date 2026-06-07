
package validation;
import exception.ValidationException;
public interface Validator<T> {
    void validate(T obj) throws ValidationException;
}
