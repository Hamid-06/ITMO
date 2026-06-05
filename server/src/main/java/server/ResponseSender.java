package server;

import network.CommandResponse;
import network.SerializeUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Logger;

public class ResponseSender {

    private static final Logger log = Logger.getLogger(ResponseSender.class.getName());

    private final DatagramSocket socket;

    public ResponseSender(DatagramSocket socket) {
        this.socket = socket;
    }

    public void send(CommandResponse response, InetAddress clientAddr, int clientPort)
            throws Exception {
        byte[] data = SerializeUtil.serialize(response);
        DatagramPacket packet = new DatagramPacket(data, data.length, clientAddr, clientPort);
        socket.send(packet);
        log.info("Response sent to " + clientAddr + ":" + clientPort +
                " | success=" + response.isSuccess() + " | bytes=" + data.length);
    }
}