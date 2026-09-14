package repository;

import db.Database;
import db.TableNames;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public final class PostgresUserRepository implements UserRepository {
    private final Database database;
    private final String table;

    public PostgresUserRepository(Database database) { this(database, TableNames.DEFAULT); }

    public PostgresUserRepository(Database database, TableNames names) {
        this.database = database;
        this.table = names.users();
    }

    @Override
    public Optional<User> findByLogin(String login) {
        return database.read(connection -> {
            try (PreparedStatement query = connection.prepareStatement(
                    "SELECT id, login, password_hash FROM " + table + " WHERE login = ?")) {
                query.setQueryTimeout(10);
                query.setString(1, login);
                try (ResultSet rows = query.executeQuery()) {
                    return rows.next() ? Optional.of(new User(
                            rows.getInt("id"), rows.getString("login"), rows.getString("password_hash"))) : Optional.empty();
                }
            }
        });
    }

    @Override
    public Optional<User> create(String login, String passwordHash) {
        return database.transaction(connection -> {
            try (PreparedStatement query = connection.prepareStatement("""
                    INSERT INTO %s (login, password_hash) VALUES (?, ?)
                    ON CONFLICT (login) DO NOTHING RETURNING id
                    """.formatted(table))) {
                query.setQueryTimeout(10);
                query.setString(1, login);
                query.setString(2, passwordHash);
                try (ResultSet rows = query.executeQuery()) {
                    return rows.next() ? Optional.of(new User(rows.getInt(1), login, passwordHash)) : Optional.empty();
                }
            }
        });
    }
}
