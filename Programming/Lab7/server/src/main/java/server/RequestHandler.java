package server;

import commands.CommandRegistry;
import db.PersistenceException;
import network.*;
import service.AuthException;
import service.AuthService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Logger;

/** Аутентификация перед диспетчеризацией команд, независимо от поведения клиента. */
public final class RequestHandler {
    private static final Logger LOG = Logger.getLogger(RequestHandler.class.getName());
    private final AuthService auth;
    private final CommandRegistry commands;
    private final RequestJournal journal = new RequestJournal();

    public RequestHandler(AuthService auth, CommandRegistry commands) {
        this.auth = auth;
        this.commands = commands;
    }

    public byte[] handle(CommandRequest request, byte[] payload) throws IOException {
        if (request.getType() == CommandType.REGISTER) {
            return journal.execute(request.getRequestId(), payload, () -> guarded(() -> {
                int userId = auth.register(request.getCredentials());
                return CommandResponse.ok("Регистрация выполнена. Ваш id=" + userId);
            }));
        }
        // Учётные данные проверяем даже при повторной доставке того же запроса.
        int userId;
        try {
            userId = auth.authenticate(request.getCredentials());
        } catch (AuthException e) {
            return RequestJournal.encode(CommandResponse.authError(e.getMessage()));
        } catch (PersistenceException e) {
            logDatabaseFailure(e);
            return RequestJournal.encode(CommandResponse.fail("БД недоступна для проверки пользователя."));
        }
        if (request.getType() == CommandType.LOGIN) {
            return RequestJournal.encode(CommandResponse.ok("Вход выполнен. Ваш id=" + userId));
        }
        return journal.execute(request.getRequestId(), payload,
                () -> guarded(() -> commands.execute(request, userId)));
    }

    private static CommandResponse guarded(Supplier<CommandResponse> action) {
        try {
            return action.get();
        } catch (AuthException e) {
            return CommandResponse.authError(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            return CommandResponse.fail(e.getMessage());
        } catch (PersistenceException e) {
            logDatabaseFailure(e);
            return CommandResponse.fail(e.isUncertain()
                    ? "Результат commit неизвестен. Нужен перезапуск сервера; не повторяйте изменение вслепую."
                    : "БД отклонила операцию. Коллекция в памяти не изменена.");
        } catch (RuntimeException e) {
            LOG.severe("Внутренняя ошибка обработчика: " + e.getClass().getSimpleName());
            return CommandResponse.fail("Внутренняя ошибка сервера.");
        }
    }

    private static void logDatabaseFailure(PersistenceException error) {
        Throwable cause = error.getCause();
        // Не выводим текст SQLException: PostgreSQL может включить данные строки в DETAIL.
        LOG.warning("Сбой БД; SQLState=" + (cause instanceof SQLException sql ? sql.getSQLState() : "unknown")
                + ", uncertain=" + error.isUncertain());
    }
}
