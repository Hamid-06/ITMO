package client;

import network.CommandRequest;
import network.CommandResponse;
import network.SerializeUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class CommandSender {

    private static final int    BUFFER_SIZE   = 65507;
    private static final int    MAX_ATTEMPTS  = 50;   // 50 × 100 ms = 5 seconds
    private static final long   RETRY_DELAY   = 100L; // ms

    private final InetSocketAddress serverAddress;

    public CommandSender(String host, int port) {
        this.serverAddress = new InetSocketAddress(host, port);
    }

    public CommandResponse send(CommandRequest request) {
        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.configureBlocking(false); // NON-BLOCKING mode

            // Serialize and send
            byte[]     bytes   = SerializeUtil.serialize(request);
            ByteBuffer sendBuf = ByteBuffer.wrap(bytes);
            channel.send(sendBuf, serverAddress);

            // Wait for response with timeout
            ByteBuffer recvBuf = ByteBuffer.allocate(BUFFER_SIZE);
            SocketAddress from  = null;
            int attempts        = 0;

            while (from == null && attempts < MAX_ATTEMPTS) {
                recvBuf.clear();
                from = channel.receive(recvBuf); // returns null immediately if no data
                if (from == null) {
                    Thread.sleep(RETRY_DELAY);
                    attempts++;
                }
            }

            if (from == null) {
                return null; // server did not respond in time
            }

            // Deserialize response
            recvBuf.flip();
            byte[] responseBytes = new byte[recvBuf.limit()];
            recvBuf.get(responseBytes);
            return (CommandResponse) SerializeUtil.deserialize(responseBytes);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            System.out.println("[ERROR] Communication error: " + e.getMessage());
            return null;
        }
    }
}