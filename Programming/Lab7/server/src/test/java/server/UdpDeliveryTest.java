package server;

import client.CommandSender;
import commands.CommandRegistry;
import models.*;
import network.*;
import org.junit.jupiter.api.Test;
import repository.UserRepository;
import service.*;
import support.MemoryRouteRepository;
import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.*;
import static org.junit.jupiter.api.Assertions.*;

class UdpDeliveryTest {
    @Test
    void lostResponseTriggersRetryWithoutDuplicateInsert() throws Exception {
        PasswordHasher hasher = new PasswordHasher();
        UserRepository.User alice = new UserRepository.User(1, "alice", hasher.hash("secret"));
        UserRepository users = new UserRepository() {
            @Override public Optional<User> findByLogin(String login) {
                return login.equals("alice") ? Optional.of(alice) : Optional.empty();
            }
            @Override public Optional<User> create(String login, String hash) { throw new UnsupportedOperationException(); }
        };
        var collection = new CollectionService(new MemoryRouteRepository());
        var handler = new RequestHandler(new AuthService(users, hasher), new CommandRegistry(collection));
        try (UdpServer server = new UdpServer(new InetSocketAddress("127.0.0.1", 0), handler);
             DatagramSocket proxy = new DatagramSocket(new InetSocketAddress("127.0.0.1", 0))) {
            server.start();
            InetSocketAddress backend = new InetSocketAddress("127.0.0.1", server.port());
            AtomicBoolean dropped = new AtomicBoolean();
            AtomicInteger requestStarts = new AtomicInteger();
            Thread forwarder = new Thread(() -> {
                SocketAddress client = null;
                while (!proxy.isClosed()) {
                    try {
                        byte[] bytes = new byte[UdpProtocol.MAX_PACKET_BYTES];
                        DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
                        proxy.receive(packet);
                        byte[] data = Arrays.copyOf(bytes, packet.getLength());
                        if (packet.getSocketAddress().equals(backend)) {
                            // Ответ ADD помещается в один фрагмент: первый ответ теряем целиком.
                            if (dropped.compareAndSet(false, true)) continue;
                            if (client != null) proxy.send(new DatagramPacket(data, data.length, client));
                        } else {
                            client = packet.getSocketAddress();
                            if (UdpProtocol.decode(data).index() == 0) requestStarts.incrementAndGet();
                            proxy.send(new DatagramPacket(data, data.length, backend));
                        }
                    } catch (IOException ignored) {
                        if (proxy.isClosed()) return;
                    }
                }
            }, "test-lossy-udp-proxy");
            forwarder.setDaemon(true);
            forwarder.start();
            try (CommandSender sender = new CommandSender("127.0.0.1", proxy.getLocalPort(), Duration.ofMillis(300), 4)) {
                var data = new RouteData("route", new Coordinates(1f, 2L), new Location(3, 4.0, 5L), null, 6L);
                var response = sender.send(new CommandRequest(CommandType.ADD, data, new Credentials("alice", "secret")));
                assertTrue(response.isSuccess());
                assertTrue(dropped.get());
                assertTrue(requestStarts.get() >= 2);
                assertEquals(1, collection.snapshot().size());
            } finally {
                proxy.close();
                forwarder.join(2000);
            }
        }
    }
}
