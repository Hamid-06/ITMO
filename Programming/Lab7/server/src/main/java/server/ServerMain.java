package server;

import commands.CommandRegistry;
import db.*;
import repository.PostgresRouteRepository;
import repository.PostgresUserRepository;
import service.AuthService;
import service.CollectionService;
import service.PasswordHasher;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;

public final class ServerMain {
    private ServerMain() { }

    public static void main(String[] args) {
        try {
            DbConfig config = DbConfig.fromEnv();
            ConnectionFactory connections = new JdbcConnectionFactory(config);
            if (args.length == 1 && args[0].equals("--check-db")) {
                checkDatabase(connections);
                return;
            }
            if (args.length > 2) throw new IllegalArgumentException("Запуск: server.jar [port] [bindAddress]");
            int port = args.length >= 1 ? Integer.parseInt(args[0])
                    : Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8080"));
            String bind = args.length == 2 ? args[1] : "0.0.0.0";
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Порт должен быть 1..65535.");
            Database database = new Database(connections);
            CollectionService collection = new CollectionService(new PostgresRouteRepository(database));
            AuthService auth = new AuthService(new PostgresUserRepository(database), new PasswordHasher());
            UdpServer server = new UdpServer(new InetSocketAddress(bind, port),
                    new RequestHandler(auth, new CommandRegistry(collection)));
            CountDownLatch stopped = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                server.close();
                stopped.countDown();
            }, "server-shutdown"));
            server.start();
            System.out.println("UDP-сервер слушает " + bind + ":" + server.port()
                    + ". Загружено маршрутов: " + collection.snapshot().size());
            System.out.println("Для завершения нажмите Ctrl+C. Изменения уже сохранены в PostgreSQL.");
            stopped.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // Не печатаем цепочку исключений с потенциальными данными подключения.
            System.err.println("Сервер не запущен: " + (e instanceof PersistenceException
                    ? "проверьте подключение к БД и таблицы users/routes." : safeMessage(e)));
            System.exit(1);
        }
    }

    private static void checkDatabase(ConnectionFactory connections) throws Exception {
        try (Connection connection = connections.open();
             PreparedStatement query = connection.prepareStatement(
                     "SELECT current_database(), current_user, current_schema()");
             ResultSet result = query.executeQuery()) {
            result.next();
            System.out.println("Подключение установлено. База: " + result.getString(1)
                    + ", пользователь: " + result.getString(2) + ", схема: " + result.getString(3));
        }
    }

    private static String safeMessage(Exception e) {
        if (e instanceof java.sql.SQLException) return "не удалось подключиться к PostgreSQL.";
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
