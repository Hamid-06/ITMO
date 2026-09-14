package integration;

import client.CommandSender;
import commands.CommandRegistry;
import db.*;
import models.*;
import network.*;
import org.junit.jupiter.api.*;
import repository.*;
import server.RequestHandler;
import server.UdpServer;
import service.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Включается LAB7_TEST_DB=true. Изолирует тесты случайным префиксом таблиц. */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostgresIntegrationTest {
    private ConnectionFactory scoped;
    private Database database;
    private TableNames tables;

    @BeforeAll
    void createIsolatedTables() throws Exception {
        assumeTrue("true".equalsIgnoreCase(System.getenv("LAB7_TEST_DB")),
                "Для реальной PostgreSQL установите LAB7_TEST_DB=true.");
        scoped = new JdbcConnectionFactory(DbConfig.fromEnv());
        tables = new TableNames("lab7_test_" + UUID.randomUUID().toString().replace("-", "") + "_");
        database = new Database(scoped);
        try (var input = getClass().getResourceAsStream("/db/schema.sql")) {
            assertNotNull(input);
            String ddl = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            try (Connection connection = scoped.open(); Statement statement = connection.createStatement()) {
                for (String sql : ddl.split(";")) if (!sql.isBlank()) statement.execute(sql(sql));
            }
        }
    }

    @AfterAll
    void dropOnlyIsolatedTables() throws Exception {
        if (scoped == null || tables == null) return;
        if (!tables.prefix().matches("lab7_test_[a-f0-9]{32}_")) throw new IllegalStateException("Unsafe cleanup target");
        try (Connection connection = scoped.open(); Statement statement = connection.createStatement()) {
            statement.execute(sql("DROP TABLE IF EXISTS route_locks"));
            statement.execute(sql("DROP TABLE IF EXISTS routes"));
            statement.execute(sql("DROP TABLE IF EXISTS temporarily_hidden_routes"));
            statement.execute(sql("DROP TABLE IF EXISTS users"));
            statement.execute(sql("DROP SEQUENCE IF EXISTS routes_id_seq, users_id_seq"));
        }
    }

    private String sql(String source) {
        return source.replaceAll("\\b(users|routes|users_id_seq|routes_id_seq|routes_owner_id_idx|"
                + "routes_to_complete|route_locks|test_distance|temporarily_hidden_routes)\\b", tables.prefix() + "$1");
    }

    @BeforeEach
    void resetTestTables() throws Exception {
        try (Connection connection = scoped.open(); Statement statement = connection.createStatement()) {
            statement.execute(sql("DROP TABLE IF EXISTS route_locks"));
            statement.execute(sql("ALTER TABLE routes DROP CONSTRAINT IF EXISTS test_distance"));
            statement.execute(sql("TRUNCATE routes, users RESTART IDENTITY"));
        }
    }

    private AuthService auth() {
        return new AuthService(new PostgresUserRepository(database, tables), new PasswordHasher());
    }

    private CollectionService collection() { return new CollectionService(new PostgresRouteRepository(database, tables)); }

    private static RouteData data(String name, Long distance) {
        return new RouteData(name, new Coordinates(-175.5f, Long.MAX_VALUE),
                new Location(Long.MIN_VALUE, 2.5, Long.MAX_VALUE), null, distance);
    }

    private long scalar(String sql) throws Exception {
        try (Connection connection = scoped.open(); Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql(sql))) {
            result.next();
            return result.getLong(1);
        }
    }

    @Test
    void postgresSequencesNullableFieldsAndRestartLoading() throws Exception {
        int owner = auth().register(new Credentials("alice", "secret"));
        var collection = collection();
        Route first = collection.add(data("nulls", null), owner, false).orElseThrow();
        Route second = collection.add(new RouteData("full", new Coordinates(1.5f, 2L),
                new Location(3, 4.5, 6L), new Location(7, 8.5, 9L), 20L), owner, false).orElseThrow();
        assertTrue(first.getId() > 0);
        assertTrue(second.getId() > first.getId());
        assertEquals(second.getId().longValue(), scalar("SELECT last_value FROM routes_id_seq"));
        assertEquals(owner, scalar("SELECT owner_id FROM routes WHERE id = " + first.getId()));
        assertEquals(2, scalar("SELECT count(*) FROM routes"));
        List<Route> loaded = collection().snapshot();
        assertEquals(2, loaded.size());
        assertNull(loaded.get(0).getDistance());
        assertNull(loaded.get(0).getTo());
        assertEquals(Long.MAX_VALUE, loaded.get(0).getCoordinates().getY());
        assertEquals(first.getCreationDate(), loaded.get(0).getCreationDate());
        assertEquals(new Location(7, 8.5, 9L), loaded.get(1).getTo());
        Route updated = collection.update(second.getId(), data("updated", 30L), owner);
        assertEquals(second.getCreationDate(), updated.getCreationDate());
        assertEquals(owner, updated.getOwnerId());
        assertEquals("updated", collection().snapshot().get(1).getName());
    }

    @Test
    void ownershipIsEnforcedInSqlAndBatchFailureRollsBack() throws Exception {
        int alice = auth().register(new Credentials("alice", "a"));
        int bob = auth().register(new Credentials("bob", "b"));
        var repository = new PostgresRouteRepository(database, tables);
        Route mine = repository.insert(data("mine", 3L), alice);
        Route other = repository.insert(data("other", 4L), bob);
        assertThrows(PersistenceException.class, () -> repository.update(other.getId(), data("stolen", 5L), alice));
        assertThrows(PersistenceException.class, () -> repository.delete(List.of(mine.getId(), other.getId()), alice));
        assertEquals(2, scalar("SELECT count(*) FROM routes"));
        var collection = collection();
        assertEquals(1, collection.clear(alice));
        assertEquals(List.of(other), collection.snapshot());
        assertEquals(1, scalar("SELECT count(*) FROM routes"));
    }

    @Test
    void databaseRejectsWritesWithoutChangingMemory() throws Exception {
        int owner = auth().register(new Credentials("alice", "secret"));
        var collection = collection();
        Route first = collection.add(data("first", 5L), owner, false).orElseThrow();
        collection.add(data("second", 6L), owner, false);
        List<Route> before = collection.snapshot();
        try (Connection connection = scoped.open(); Statement statement = connection.createStatement()) {
            statement.execute(sql("ALTER TABLE routes ADD CONSTRAINT test_distance CHECK (distance < 100)"));
            statement.execute(sql("CREATE TABLE route_locks (route_id INTEGER REFERENCES routes(id))"));
            statement.execute(sql("INSERT INTO route_locks VALUES (" + first.getId() + ")"));
        }
        assertThrows(PersistenceException.class, () -> collection.add(data("bad", 999L), owner, false));
        assertThrows(PersistenceException.class, () -> collection.update(first.getId(), data("bad", 999L), owner));
        assertThrows(PersistenceException.class, () -> collection.clear(owner));
        assertEquals(before, collection.snapshot());
        assertEquals(2, scalar("SELECT count(*) FROM routes"));
        assertEquals("first", collection().snapshot().get(0).getName());
    }

    @Test
    void concurrentRegistrationAndAddsStayConsistent() throws Exception {
        var auth = auth();
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            List<Callable<Boolean>> registrations = new ArrayList<>();
            for (int i = 0; i < 12; i++) registrations.add(() -> {
                try {
                    auth.register(new Credentials("same-login", "secret"));
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            });
            int successes = 0;
            for (Future<Boolean> future : pool.invokeAll(registrations)) if (future.get()) successes++;
            assertEquals(1, successes);
            assertEquals(1, scalar("SELECT count(*) FROM users"));
            int owner = auth.authenticate(new Credentials("same-login", "secret"));
            var collection = collection();
            List<Callable<Route>> additions = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                int index = i;
                additions.add(() -> collection.add(data("route-" + index, 10L), owner, false).orElseThrow());
            }
            Set<Integer> ids = new HashSet<>();
            for (Future<Route> future : pool.invokeAll(additions)) ids.add(future.get().getId());
            assertEquals(24, ids.size());
            assertEquals(24, collection.snapshot().size());
            assertEquals(24, scalar("SELECT count(*) FROM routes"));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void collectionReadCommandsUseCacheEvenWhenRoutesTableIsUnavailable() throws Exception {
        var credentials = new Credentials("alice", "secret");
        var auth = auth();
        int owner = auth.register(credentials);
        var collection = collection();
        collection.add(data("cached", 4L), owner, false);
        var handler = new RequestHandler(auth, new CommandRegistry(collection));
        try (Connection connection = scoped.open(); Statement statement = connection.createStatement()) {
            statement.execute(sql("ALTER TABLE routes RENAME TO temporarily_hidden_routes"));
            try {
                for (CommandType type : List.of(CommandType.SHOW, CommandType.INFO, CommandType.HELP,
                        CommandType.GROUP_COUNTING_BY_COORDINATES, CommandType.FILTER_CONTAINS_NAME)) {
                    var request = new CommandRequest(type, null, "cached", null, credentials);
                    var result = SerializeUtil.deserialize(handler.handle(request, SerializeUtil.serialize(request)),
                            CommandResponse.class);
                    assertTrue(result.isSuccess(), type.name());
                }
            } finally {
                statement.execute(sql("ALTER TABLE temporarily_hidden_routes RENAME TO routes"));
            }
        }
    }

    @Test
    void realUdpAuthenticationRetryFragmentationAndOwnership() throws Exception {
        var collection = collection();
        var handler = new RequestHandler(auth(), new CommandRegistry(collection));
        try (UdpServer server = new UdpServer(new InetSocketAddress("127.0.0.1", 0), handler)) {
            server.start();
            try (CommandSender alice = new CommandSender("127.0.0.1", server.port(), Duration.ofSeconds(5), 3);
                 CommandSender bob = new CommandSender("127.0.0.1", server.port(), Duration.ofSeconds(5), 3)) {
                var a = new Credentials("alice", "secret-a");
                var b = new Credentials("bob", "secret-b");
                assertEquals(CommandResponse.Status.AUTH_ERROR,
                        alice.send(new CommandRequest(CommandType.SHOW, a)).getStatus());
                assertTrue(alice.send(new CommandRequest(CommandType.REGISTER, a)).isSuccess());
                assertTrue(bob.send(new CommandRequest(CommandType.REGISTER, b)).isSuccess());
                // Запрос и ответ значительно больше предела одной UDP-датаграммы (65507).
                var request = new CommandRequest(CommandType.ADD, data("Я".repeat(80_000), 8L), a);
                assertTrue(alice.send(request).isSuccess());
                assertTrue(alice.send(request).isSuccess());
                assertEquals(1, scalar("SELECT count(*) FROM routes"));
                var show = bob.send(new CommandRequest(CommandType.SHOW, b));
                assertTrue(show.isSuccess());
                assertEquals(1, show.getRoutes().size());
                Route route = show.getRoutes().get(0);
                assertEquals(80_000, route.getName().length());
                assertFalse(bob.send(new CommandRequest(CommandType.REMOVE_BY_ID, route.getId().longValue(), b)).isSuccess());
                assertTrue(bob.send(new CommandRequest(CommandType.CLEAR, b)).isSuccess());
                assertEquals(1, scalar("SELECT count(*) FROM routes"));
                assertTrue(alice.send(new CommandRequest(CommandType.REMOVE_BY_ID, route.getId().longValue(), a)).isSuccess());
                assertEquals(0, scalar("SELECT count(*) FROM routes"));
            }
        }
    }
}
