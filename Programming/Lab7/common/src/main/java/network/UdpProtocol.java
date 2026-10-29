package network;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;

/** Фрагментация позволяет show возвращать больше одного UDP-пакета. */
public final class UdpProtocol {
    public static final int MAX_MESSAGE_BYTES = 2 * 1024 * 1024;
    public static final int CHUNK_BYTES = 1200;
    public static final int HEADER_BYTES = 32;
    public static final int MAX_PACKET_BYTES = HEADER_BYTES + CHUNK_BYTES;
    private static final int MAGIC = 0x4C414237; // LAB7, версия протокола 1

    private UdpProtocol() { }

    public record Packet(UUID id, int index, int count, int totalBytes, byte[] payload) { }
    public record Message(UUID id, byte[] payload) { }

    public static List<byte[]> split(UUID id, byte[] message) throws IOException {
        if (message.length == 0 || message.length > MAX_MESSAGE_BYTES) {
            throw new IOException("Недопустимый размер сообщения.");
        }
        int count = (message.length + CHUNK_BYTES - 1) / CHUNK_BYTES;
        List<byte[]> packets = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int offset = index * CHUNK_BYTES;
            int size = Math.min(CHUNK_BYTES, message.length - offset);
            packets.add(ByteBuffer.allocate(HEADER_BYTES + size)
                    .putInt(MAGIC).putLong(id.getMostSignificantBits()).putLong(id.getLeastSignificantBits())
                    .putInt(index).putInt(count).putInt(message.length)
                    .put(message, offset, size).array());
        }
        return packets;
    }

    public static Packet decode(byte[] data) throws IOException {
        if (data.length <= HEADER_BYTES || data.length > MAX_PACKET_BYTES) {
            throw new IOException("Неверная длина UDP-пакета.");
        }
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if (buffer.getInt() != MAGIC) throw new IOException("Неизвестный протокол.");
        UUID id = new UUID(buffer.getLong(), buffer.getLong());
        int index = buffer.getInt();
        int count = buffer.getInt();
        int total = buffer.getInt();
        if (total <= 0 || total > MAX_MESSAGE_BYTES || count != (total + CHUNK_BYTES - 1) / CHUNK_BYTES
                || index < 0 || index >= count) {
            throw new IOException("Неверный заголовок UDP-пакета.");
        }
        int expected = Math.min(CHUNK_BYTES, total - index * CHUNK_BYTES);
        if (buffer.remaining() != expected) throw new IOException("Повреждённый фрагмент.");
        byte[] payload = new byte[expected];
        buffer.get(payload);
        return new Packet(id, index, count, total, payload);
    }

    /** Общая квота ограничивает память даже для незаконченных сообщений. */
    public static final class Reassembler {
        private record Key(SocketAddress peer, UUID id) { }
        private static final long TTL_NANOS = Duration.ofSeconds(30).toNanos();
        private static final int MAX_PENDING = 64;
        private static final int MAX_RESERVED = 16 * 1024 * 1024;
        private final Map<Key, Assembly> pending = new HashMap<>();
        private int reserved;

        public synchronized Optional<Message> accept(SocketAddress peer, Packet packet) throws IOException {
            expire();
            Key key = new Key(peer, packet.id());
            Assembly assembly = pending.get(key);
            if (assembly == null) {
                if (pending.size() >= MAX_PENDING || reserved + packet.totalBytes() > MAX_RESERVED) {
                    throw new IOException("Лимит незавершённых сообщений.");
                }
                assembly = new Assembly(packet);
                pending.put(key, assembly);
                reserved += packet.totalBytes();
            }
            if (assembly.total != packet.totalBytes() || assembly.parts.length != packet.count()) {
                discard(key, assembly);
                throw new IOException("Несогласованные фрагменты.");
            }
            byte[] previous = assembly.parts[packet.index()];
            if (previous != null && !Arrays.equals(previous, packet.payload())) {
                discard(key, assembly);
                throw new IOException("Конфликтующие фрагменты.");
            }
            if (previous == null) {
                assembly.parts[packet.index()] = packet.payload();
                assembly.received++;
            }
            if (assembly.received != assembly.parts.length) return Optional.empty();
            ByteBuffer result = ByteBuffer.allocate(assembly.total);
            for (byte[] part : assembly.parts) result.put(part);
            discard(key, assembly);
            return Optional.of(new Message(packet.id(), result.array()));
        }

        public synchronized void expire() {
            long now = System.nanoTime();
            Iterator<Assembly> iterator = pending.values().iterator();
            while (iterator.hasNext()) {
                Assembly assembly = iterator.next();
                if (now - assembly.created >= TTL_NANOS) {
                    reserved -= assembly.total;
                    iterator.remove();
                }
            }
        }

        private void discard(Key key, Assembly assembly) {
            pending.remove(key);
            reserved -= assembly.total;
        }

        private static final class Assembly {
            private final byte[][] parts;
            private final int total;
            private final long created = System.nanoTime();
            private int received;

            private Assembly(Packet packet) {
                parts = new byte[packet.count()][];
                total = packet.totalBytes();
            }
        }
    }
}
