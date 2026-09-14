package server;

import commands.CommandRegistry;
import models.*;
import network.*;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import service.*;
import support.MemoryRouteRepository;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {
    private static class Users implements UserRepository {
        private final Map<String, User> users = new ConcurrentHashMap<>();
        private final AtomicInteger ids = new AtomicInteger();
        @Override public Optional<User> findByLogin(String login) { return Optional.ofNullable(users.get(login)); }
        @Override public Optional<User> create(String login, String hash) {
            User user = new User(ids.incrementAndGet(), login, hash);
            return users.putIfAbsent(login, user) == null ? Optional.of(user) : Optional.empty();
        }
    }

    private final Users users = new Users();
    private final CollectionService collection = new CollectionService(new MemoryRouteRepository());
    private final RequestHandler handler = new RequestHandler(
            new AuthService(users, new PasswordHasher()), new CommandRegistry(collection));
    private final Credentials credentials = new Credentials("alice", "secret");

    private CommandResponse send(CommandRequest request) throws Exception {
        return SerializeUtil.deserialize(handler.handle(request, SerializeUtil.serialize(request)), CommandResponse.class);
    }

    private CommandRequest add(UUID id) {
        return new CommandRequest(id, CommandType.ADD, new RouteData("route",
                new Coordinates(0f, 1L), new Location(1, 2.0, 3L), null, 3L), null, null, credentials);
    }

    @Test
    void unauthenticatedUserCannotRunAnyCollectionCommand() throws Exception {
        for (CommandType type : CommandType.values()) {
            if (type == CommandType.LOGIN || type == CommandType.REGISTER) continue;
            assertEquals(CommandResponse.Status.AUTH_ERROR,
                    send(new CommandRequest(type, credentials)).getStatus(), type.name());
        }
        assertTrue(collection.snapshot().isEmpty());
    }

    @Test
    void registrationStoresSaltedSha256AndSupportsEmptyPassword() throws Exception {
        var blank = new Credentials("blank", "");
        assertTrue(send(new CommandRequest(CommandType.REGISTER, blank)).isSuccess());
        assertTrue(send(new CommandRequest(CommandType.LOGIN, blank)).isSuccess());
        String hash = users.findByLogin("blank").orElseThrow().passwordHash();
        assertTrue(hash.matches("[0-9a-f]{32}:[0-9a-f]{64}"));
        assertTrue(new PasswordHasher().verify("", hash));
        assertFalse(new PasswordHasher().verify("wrong", hash));
        assertFalse(send(new CommandRequest(CommandType.REGISTER, blank)).isSuccess());
    }

    @Test
    void repeatedRegistrationReturnsOriginalSuccess() throws Exception {
        var register = new CommandRequest(CommandType.REGISTER, credentials);
        assertTrue(send(register).isSuccess());
        assertTrue(send(register).isSuccess());
    }

    @Test
    void concurrentRetriesExecuteAddOnce() throws Exception {
        assertTrue(send(new CommandRequest(CommandType.REGISTER, credentials)).isSuccess());
        var request = add(UUID.randomUUID());
        ExecutorService pool = Executors.newCachedThreadPool();
        try {
            List<Callable<CommandResponse>> jobs = new ArrayList<>();
            for (int i = 0; i < 20; i++) jobs.add(() -> send(request));
            for (Future<CommandResponse> future : pool.invokeAll(jobs)) assertTrue(future.get().isSuccess());
            assertEquals(1, collection.snapshot().size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void reusedIdWithDifferentPayloadIsRejectedAndPasswordIsCheckedAgain() throws Exception {
        send(new CommandRequest(CommandType.REGISTER, credentials));
        var request = add(UUID.randomUUID());
        assertTrue(send(request).isSuccess());
        var conflicting = new CommandRequest(request.getRequestId(), CommandType.CLEAR, null, null, null, credentials);
        assertFalse(send(conflicting).isSuccess());
        var badPassword = new CommandRequest(request.getRequestId(), CommandType.ADD, request.getRouteArg(), null,
                null, new Credentials("alice", "wrong"));
        assertEquals(CommandResponse.Status.AUTH_ERROR, send(badPassword).getStatus());
        assertEquals(1, collection.snapshot().size());
    }

    @Test
    void passwordHashIsSaltedAndUsesSha256() throws Exception {
        PasswordHasher hasher = new PasswordHasher();
        String first = hasher.hash("пароль");
        String second = hasher.hash("пароль");
        assertNotEquals(first, second);
        String[] parts = first.split(":");
        var digest = java.security.MessageDigest.getInstance("SHA-256");
        digest.update(HexFormat.of().parseHex(parts[0]));
        assertEquals(parts[1], HexFormat.of().formatHex(digest.digest("пароль".getBytes(java.nio.charset.StandardCharsets.UTF_8))));
        assertTrue(hasher.verify("пароль", first));
        assertFalse(hasher.verify("wrong", first));
        assertFalse(hasher.verify("пароль", "malformed"));
    }
}
