public class MovementException extends RuntimeException {
    public MovementException(String message) {

        super(message);
    }
    @Override
    public String getMessage() {
        return "Ошибка перемещения: " + super.getMessage();
    }
}
