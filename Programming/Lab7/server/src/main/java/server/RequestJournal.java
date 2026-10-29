package server;

import network.CommandResponse;
import network.SerializeUtil;
import network.UdpProtocol;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/** Повтор UDP-запроса получает прежний ответ и не выполняет INSERT повторно. */
public final class RequestJournal {
    private static final long TTL = Duration.ofMinutes(5).toNanos();
    private static final int MAX_ENTRIES = 2048;
    private static final int MAX_BYTES = 32 * 1024 * 1024;
    private final Map<UUID, Entry> entries = new HashMap<>();
    private int reservedBytes;

    public byte[] execute(UUID id, byte[] payload, Supplier<CommandResponse> action) throws IOException {
        byte[] fingerprint = fingerprint(payload);
        Entry entry;
        boolean creator;
        synchronized (this) {
            purgeExpired();
            entry = entries.get(id);
            creator = entry == null;
            if (creator) {
                if (entries.size() >= MAX_ENTRIES || reservedBytes + UdpProtocol.MAX_MESSAGE_BYTES > MAX_BYTES) {
                    return encode(CommandResponse.fail("Сервер занят. Повторите команду позже."));
                }
                entry = new Entry(fingerprint);
                entries.put(id, entry);
                reservedBytes += entry.bytes;
            } else if (!MessageDigest.isEqual(entry.fingerprint, fingerprint)) {
                return encode(CommandResponse.fail("UUID запроса уже использован с другими данными."));
            }
        }
        if (creator) {
            try {
                byte[] response = encode(action.get());
                synchronized (this) {
                    reservedBytes -= entry.bytes - response.length;
                    entry.bytes = response.length;
                    entry.finishedAt = System.nanoTime();
                    entry.response.complete(response);
                }
            } catch (RuntimeException | IOException e) {
                synchronized (this) {
                    reservedBytes -= entry.bytes;
                    entry.bytes = 0;
                    entry.finishedAt = System.nanoTime();
                    entry.response.completeExceptionally(e);
                }
                throw e;
            }
        }
        return entry.response.join();
    }

    public static byte[] encode(CommandResponse response) throws IOException {
        try {
            return SerializeUtil.serialize(response);
        } catch (IOException tooLarge) {
            return SerializeUtil.serialize(CommandResponse.fail(
                    "Ответ превышает лимит 2 МиБ. Для просмотра используйте filter_contains_name."));
        }
    }

    private void purgeExpired() {
        long now = System.nanoTime();
        Iterator<Entry> iterator = entries.values().iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (entry.response.isDone() && now - entry.finishedAt >= TTL) {
                reservedBytes -= entry.bytes;
                iterator.remove();
            }
        }
    }

    private static byte[] fingerprint(byte[] payload) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(payload);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class Entry {
        private final byte[] fingerprint;
        private final CompletableFuture<byte[]> response = new CompletableFuture<>();
        private int bytes = UdpProtocol.MAX_MESSAGE_BYTES;
        private long finishedAt;

        private Entry(byte[] fingerprint) { this.fingerprint = fingerprint; }
    }
}
