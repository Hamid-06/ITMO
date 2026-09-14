package db;

/** Изолированное пространство таблиц, например для интеграционных тестов в studs. */
public record TableNames(String prefix) {
    public static final TableNames DEFAULT = new TableNames("");

    public TableNames {
        if (prefix == null || (!prefix.isEmpty() && !prefix.matches("[a-z][a-z0-9_]{0,42}"))) {
            throw new IllegalArgumentException("Недопустимый префикс таблиц.");
        }
    }

    public String users() { return prefix + "users"; }
    public String routes() { return prefix + "routes"; }
}
