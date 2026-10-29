package db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Конфигурация подключения. Пароль не записывается в исходники или сообщения. */
public final class DbConfig {
    private final String url;
    private final String user;
    private final String password;

    public DbConfig(String url, String user, String password) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Задайте DB_URL.");
        if (!url.startsWith("jdbc:postgresql://")) throw new IllegalArgumentException("Нужен JDBC URL PostgreSQL.");
        if (user == null || user.isBlank()) throw new IllegalArgumentException("Задайте DB_USER.");
        if (password == null) throw new IllegalArgumentException("Задайте DB_PASSWORD.");
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String getUrl() { return url; }
    public String getUser() { return user; }
    public String getPassword() { return password; }

    /** Приоритет: -Ddb.* > DB_* > локальный файл > значение по умолчанию. */
    public static DbConfig fromEnv() {
        Properties local = new Properties();
        String explicit = setting("db.config", "DB_CONFIG");
        Path file = explicit == null ? findLocalConfig() : Path.of(explicit);
        if (file != null) {
            try (InputStream input = Files.newInputStream(file)) {
                local.load(input);
            } catch (IOException e) {
                throw new IllegalArgumentException("Не удалось прочитать файл настроек БД: " + file, e);
            }
        }
        return new DbConfig(
                choose(setting("db.url", "DB_URL"), local.getProperty("url"), "jdbc:postgresql://localhost:9000/studs"),
                choose(setting("db.user", "DB_USER"), local.getProperty("user"), null),
                choose(setting("db.password", "DB_PASSWORD"), local.getProperty("password"), null));
    }

    private static String setting(String property, String environment) {
        String value = System.getProperty(property);
        return value != null ? value : System.getenv(environment);
    }

    private static String choose(String preferred, String fallback, String defaultValue) {
        return preferred != null ? preferred : fallback != null ? fallback : defaultValue;
    }

    private static Path findLocalConfig() {
        Path directory = Path.of("").toAbsolutePath();
        for (int level = 0; directory != null && level < 4; level++, directory = directory.getParent()) {
            Path candidate = directory.resolve("db.local.properties");
            if (Files.isRegularFile(candidate)) return candidate;
        }
        return null;
    }
}
