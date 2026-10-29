package repository;

import java.util.Optional;

public interface UserRepository {
    record User(int id, String login, String passwordHash) {
        @Override public String toString() { return "User{id=" + id + ", login='" + login + "'}"; }
    }

    Optional<User> findByLogin(String login);
    Optional<User> create(String login, String passwordHash);
}
