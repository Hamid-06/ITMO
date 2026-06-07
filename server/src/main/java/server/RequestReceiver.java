package server;

import network.CommandRequest;
import network.SerializeUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class RequestReceiver {

    private static final Logger log = Logger.getLogger(RequestReceiver.class.getName());
    private static final int BUFFER_SIZE = 65507;

    private final DatagramSocket socket;

    public RequestReceiver(DatagramSocket socket) {
        this.socket = socket;
    }

    public ReceivedRequest receive() throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);  // BLOCKS until data arrives

        InetAddress clientAddr = packet.getAddress();
        int clientPort = packet.getPort();

        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());

        CommandRequest request = (CommandRequest) SerializeUtil.deserialize(data);
        log.info("Received request from " + clientAddr + ":" + clientPort +
                " | command: " + request.getType());
        return new ReceivedRequest(request, clientAddr, clientPort);
    }

    public static class ReceivedRequest {
        public final CommandRequest request;
        public final InetAddress clientAddr;
        public final int clientPort;

        public ReceivedRequest(CommandRequest request, InetAddress addr, int port) {
            this.request = request;
            this.clientAddr = addr;
            this.clientPort = port;
        }
    }
}