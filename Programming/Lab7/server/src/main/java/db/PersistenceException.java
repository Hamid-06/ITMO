package db;

/** uncertain=true: после потери связи неизвестно, успел ли сервер БД выполнить commit. */
public final class PersistenceException extends RuntimeException {
    private final boolean uncertain;

    public PersistenceException(String message, Throwable cause, boolean uncertain) {
        super(message, cause);
        this.uncertain = uncertain;
    }

    public boolean isUncertain() { return uncertain; }
}
