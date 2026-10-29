package server;

import network.*;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Чтение — новый Thread; обработка — cached pool; отправка — отдельный cached pool.
 * Один reader читает сокет, не смешивая адреса и содержимое разных датаграмм.
 */
public final class UdpServer implements AutoCloseable {
    private static final Logger LOG = Logger.getLogger(UdpServer.class.getName());
    private final DatagramSocket socket;
    private final RequestHandler handler;
    private final UdpProtocol.Reassembler reassembler = new UdpProtocol.Reassembler();
    private final ExecutorService processing = Executors.newCachedThreadPool();
    private final ExecutorService sending = Executors.newCachedThreadPool();
    private final Semaphore inFlight = new Semaphore(64);
    private final AtomicBoolean closed = new AtomicBoolean();
    private final Thread reader;

    public UdpServer(InetSocketAddress address, RequestHandler handler) throws SocketException {
        this.handler = handler;
        socket = new DatagramSocket(null);
        try {
            socket.setReceiveBufferSize(4 * 1024 * 1024);
            socket.setSendBufferSize(1024 * 1024);
            socket.bind(address);
            socket.setSoTimeout(1000);
        } catch (SocketException e) {
            socket.close();
            throw e;
        }
        reader = new Thread(this::readRequests, "udp-request-reader");
    }

    public void start() { reader.start(); }
    public int port() { return socket.getLocalPort(); }

    private void readRequests() {
        while (!closed.get()) {
            try {
                // +1 позволяет обнаружить усечённые пакеты, превышающие размер протокола.
                byte[] buffer = new byte[UdpProtocol.MAX_PACKET_BYTES + 1];
                DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagram);
                SocketAddress peer = datagram.getSocketAddress();
                UdpProtocol.Packet packet = UdpProtocol.decode(Arrays.copyOf(buffer, datagram.getLength()));
                var message = reassembler.accept(peer, packet);
                if (message.isEmpty()) continue;
                UdpProtocol.Message complete = message.get();
                if (!inFlight.tryAcquire()) continue; // Клиент повторит тот же UUID.
                try {
                    new Thread(() -> readCommand(peer, complete), "request-read-" + complete.id()).start();
                } catch (RejectedExecutionException e) {
                    inFlight.release();
                }
            } catch (SocketTimeoutException e) {
                reassembler.expire();
            } catch (IOException e) {
                if (closed.get()) break;
                LOG.fine("Некорректный или потерянный UDP-пакет.");
            }
        }
    }

    private void readCommand(SocketAddress peer, UdpProtocol.Message message) {
        try {
            CommandRequest request = SerializeUtil.deserialize(message.payload(), CommandRequest.class);
            if (!message.id().equals(request.getRequestId())) throw new IOException("UUID не совпадает.");
            processing.execute(() -> process(peer, message, request));
        } catch (IOException | RuntimeException e) {
            reject(peer, message.id());
        }
    }

    private void process(SocketAddress peer, UdpProtocol.Message message, CommandRequest request) {
        try {
            byte[] response = handler.handle(request, message.payload());
            scheduleSend(peer, message.id(), response);
        } catch (IOException | RuntimeException e) {
            reject(peer, message.id());
        }
    }

    private void reject(SocketAddress peer, UUID id) {
        try {
            scheduleSend(peer, id,
                    RequestJournal.encode(CommandResponse.fail("Некорректный запрос или внутренняя ошибка.")));
        } catch (IOException | RejectedExecutionException ignored) {
            inFlight.release();
        }
    }

    private void scheduleSend(SocketAddress peer, UUID id, byte[] response) {
        sending.execute(() -> {
            try {
                int sent = 0;
                for (byte[] packet : UdpProtocol.split(id, response)) {
                    socket.send(new DatagramPacket(packet, packet.length, peer));
                    if (++sent % 32 == 0) Thread.sleep(1);
                }
            } catch (IOException e) {
                if (!closed.get()) LOG.fine("Ответ не отправлен; клиент может повторить запрос.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                inFlight.release();
            }
        });
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;
        socket.close();
        // Сначала даём начатым транзакциям завершиться; новых запросов уже нет.
        processing.shutdown();
        await(processing);
        sending.shutdown();
        await(sending);
        try {
            reader.join(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void await(ExecutorService executor) {
        try {
            if (!executor.awaitTermination(20, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
