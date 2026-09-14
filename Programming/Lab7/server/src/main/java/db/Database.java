package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Semaphore;

/** Общий шаблон транзакции: open -> work -> commit; при ошибке rollback. */
public final class Database {
    @FunctionalInterface
    public interface SqlWork<T> { T run(Connection connection) throws SQLException; }

    private final ConnectionFactory connections;
    private final Semaphore slots = new Semaphore(16, true);

    public Database(ConnectionFactory connections) { this.connections = connections; }

    public <T> T read(SqlWork<T> work) { return execute(work, false); }
    public <T> T transaction(SqlWork<T> work) { return execute(work, true); }

    private <T> T execute(SqlWork<T> work, boolean write) {
        boolean acquired = false;
        Connection connection = null;
        boolean commitStarted = false;
        boolean committed = false;
        try {
            slots.acquire();
            acquired = true;
            connection = connections.open();
            connection.setAutoCommit(false);
            connection.setReadOnly(!write);
            T result = work.run(connection);
            commitStarted = true;
            connection.commit();
            committed = true;
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenceException("Операция БД прервана.", e, false);
        } catch (SQLException | RuntimeException e) {
            boolean uncertain = write && commitStarted;
            if (connection != null && !committed) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                    uncertain = write;
                }
            }
            throw new PersistenceException("Операция БД не выполнена.", e, uncertain);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                    // Успешный commit уже состоялся; ошибка close не отменяет результат.
                }
            }
            if (acquired) slots.release();
        }
    }
}
