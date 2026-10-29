package service;

import network.Credentials;
import repository.UserRepository;

public final class AuthService {
    private final UserRepository users;
    private final PasswordHasher passwords;
    private final String dummyHash;

    public AuthService(UserRepository users, PasswordHasher passwords) {
        this.users = users;
        this.passwords = passwords;
        this.dummyHash = passwords.hash("timing-placeholder");
    }

    public int register(Credentials credentials) {
        return users.create(credentials.getLogin(), passwords.hash(credentials.getPassword()))
                .orElseThrow(() -> new IllegalArgumentException("Этот логин уже занят.")).id();
    }

    /** Вызывается для каждого запроса; клиент не может сообщить свой ownerId. */
    public int authenticate(Credentials credentials) {
        if (credentials == null) throw new AuthException("Нужны логин и пароль.");
        var user = users.findByLogin(credentials.getLogin());
        boolean valid = passwords.verify(credentials.getPassword(),
                user.map(UserRepository.User::passwordHash).orElse(dummyHash));
        if (user.isEmpty() || !valid) throw new AuthException("Неверный логин или пароль.");
        return user.get().id();
    }
}
