package client;

import network.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/** Не блокирующий DatagramChannel; повторы используют один и тот же UUID. */
public final class CommandSender implements AutoCloseable {
    private final DatagramChannel channel;
    private final Selector selector;
    private final InetSocketAddress server;
    private final Duration timeout;
    private final int attempts;

    public CommandSender(String host, int port) throws IOException {
        this(host, port, Duration.ofSeconds(10), 3);
    }

    public CommandSender(String host, int port, Duration timeout, int attempts) throws IOException {
        if (timeout.isNegative() || timeout.isZero() || attempts < 1) throw new IllegalArgumentException();
        this.server = new InetSocketAddress(host, port);
        this.timeout = timeout;
        this.attempts = attempts;
        channel = DatagramChannel.open();
        Selector openedSelector = null;
        try {
            channel.configureBlocking(false);
            channel.setOption(java.net.StandardSocketOptions.SO_RCVBUF, 4 * 1024 * 1024);
            channel.setOption(java.net.StandardSocketOptions.SO_SNDBUF, 1024 * 1024);
            channel.connect(server);
            openedSelector = Selector.open();
            channel.register(openedSelector, SelectionKey.OP_READ);
            selector = openedSelector;
        } catch (IOException | RuntimeException e) {
            channel.close();
            if (openedSelector != null) openedSelector.close();
            throw e;
        }
    }

    public synchronized CommandResponse send(CommandRequest request) throws IOException {
        List<byte[]> packets = UdpProtocol.split(request.getRequestId(), SerializeUtil.serialize(request));
        UdpProtocol.Reassembler assembler = new UdpProtocol.Reassembler();
        ByteBuffer buffer = ByteBuffer.allocate(UdpProtocol.MAX_PACKET_BYTES + 1);
        for (int attempt = 0; attempt < attempts; attempt++) {
            sendPackets(packets);
            long deadline = System.nanoTime() + timeout.toNanos();
            while (System.nanoTime() < deadline) {
                if (Thread.currentThread().isInterrupted()) throw new IOException("Ожидание ответа прервано.");
                selector.select(Math.max(1, Math.min(1000, (deadline - System.nanoTime()) / 1_000_000)));
                selector.selectedKeys().clear();
                buffer.clear();
                while (channel.read(buffer) > 0) {
                    byte[] data = Arrays.copyOf(buffer.array(), buffer.position());
                    buffer.clear();
                    UdpProtocol.Packet packet;
                    try {
                        packet = UdpProtocol.decode(data);
                    } catch (IOException ignored) {
                        continue;
                    }
                    if (!packet.id().equals(request.getRequestId())) continue;
                    var message = assembler.accept(server, packet);
                    if (message.isPresent()) {
                        return SerializeUtil.deserialize(message.get().payload(), CommandResponse.class);
                    }
                }
            }
        }
        throw new SocketTimeoutException("Сервер не ответил после " + attempts
                + " попыток. Результат изменения неизвестен; проверьте show после восстановления связи.");
    }

    private void sendPackets(List<byte[]> packets) throws IOException {
        long deadline = System.nanoTime() + timeout.toNanos();
        int count = 0;
        for (byte[] packet : packets) {
            ByteBuffer buffer = ByteBuffer.wrap(packet);
            while (channel.write(buffer) == 0) {
                if (System.nanoTime() >= deadline) throw new SocketTimeoutException("Не удалось отправить запрос.");
                pause();
            }
            if (++count % 32 == 0) pause();
        }
    }

    private static void pause() throws IOException {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Отправка прервана.", e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            channel.close();
        } finally {
            selector.close();
        }
    }
}
