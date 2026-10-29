package network;

import java.io.Serial;
import java.io.Serializable;

public final class Credentials implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final String login;
    private final String password;

    public Credentials(String login, String password) {
        if (login == null || login.isBlank()) throw new IllegalArgumentException("Логин обязателен.");
        if (login.length() > 128) throw new IllegalArgumentException("Логин длиннее 128 символов.");
        if (password == null) throw new IllegalArgumentException("Пароль не может быть null.");
        if (password.length() > 1024) throw new IllegalArgumentException("Пароль длиннее 1024 символов.");
        this.login = login;
        this.password = password;
    }

    public String getLogin() { return login; }
    public String getPassword() { return password; }

    @Serial
    private Object readResolve() { return new Credentials(login, password); }

    @Override
    public String toString() { return "Credentials{login='" + login + "'}"; }
}
