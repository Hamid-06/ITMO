public class ActionException extends RuntimeException {
    public ActionException(String message) {

        super(message);
    }
    @Override
    public String getMessage() {
        return "Ошибка действия агента: " + super.getMessage();
    }
}
