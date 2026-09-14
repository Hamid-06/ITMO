package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Каждый вызов возвращает отдельное соединение; потоки не делят Connection. */
public final class JdbcConnectionFactory implements ConnectionFactory {
    private final DbConfig config;

    public JdbcConnectionFactory(DbConfig config) { this.config = config; }

    @Override
    public Connection open() throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", config.getUser());
        properties.setProperty("password", config.getPassword());
        properties.setProperty("connectTimeout", "5");
        properties.setProperty("socketTimeout", "15");
        properties.setProperty("tcpKeepAlive", "true");
        properties.setProperty("ApplicationName", "lab7");
        return DriverManager.getConnection(config.getUrl(), properties);
    }
}
