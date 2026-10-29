package network;

import models.*;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {
    private static final InetSocketAddress PEER = new InetSocketAddress("127.0.0.1", 12345);

    @Test
    void requestRoundTripPreservesCredentialsAndNullableFields() throws Exception {
        var data = new RouteData("Москва", new Coordinates(0f, 1L), new Location(1, 2.5, 3L), null, null);
        var original = new CommandRequest(CommandType.ADD, data, new Credentials("user", ""));
        var decoded = SerializeUtil.deserialize(SerializeUtil.serialize(original), CommandRequest.class);
        assertEquals(original.getRequestId(), decoded.getRequestId());
        assertEquals("user", decoded.getCredentials().getLogin());
        assertEquals("", decoded.getCredentials().getPassword());
        assertNull(decoded.getRouteArg().getTo());
        assertNull(decoded.getRouteArg().getDistance());
    }

    @Test
    void responseRoundTripPreservesRoutesAndOwner() throws Exception {
        var route = new Route(1, "route", new Coordinates(0f, 1L), new Date(1234),
                new Location(1, 2.0, 3L), null, 7L, 42);
        var response = CommandResponse.ok("done", List.of(route));
        var decoded = SerializeUtil.deserialize(SerializeUtil.serialize(response), CommandResponse.class);
        assertEquals(42, decoded.getRoutes().get(0).getOwnerId());
        assertEquals(1234, decoded.getRoutes().get(0).getCreationDate().getTime());
        assertThrows(UnsupportedOperationException.class, () -> decoded.getRoutes().clear());
    }

    @Test
    void largeOutOfOrderMessageAcceptsDuplicates() throws Exception {
        byte[] content = new byte[200_000];
        new Random(1).nextBytes(content);
        UUID id = UUID.randomUUID();
        List<byte[]> packets = UdpProtocol.split(id, content);
        Collections.reverse(packets);
        var assembler = new UdpProtocol.Reassembler();
        assertTrue(assembler.accept(PEER, UdpProtocol.decode(packets.get(0))).isEmpty());
        UdpProtocol.Message result = null;
        for (byte[] packet : packets) {
            var completed = assembler.accept(PEER, UdpProtocol.decode(packet));
            if (completed.isPresent()) result = completed.get();
        }
        assertNotNull(result);
        assertEquals(id, result.id());
        assertArrayEquals(content, result.payload());
    }

    @Test
    void fragmentsFromDifferentPeersCannotMix() throws Exception {
        var packets = UdpProtocol.split(UUID.randomUUID(), new byte[2000]);
        var assembler = new UdpProtocol.Reassembler();
        assertTrue(assembler.accept(PEER, UdpProtocol.decode(packets.get(0))).isEmpty());
        assertTrue(assembler.accept(new InetSocketAddress("127.0.0.1", 12346),
                UdpProtocol.decode(packets.get(1))).isEmpty());
        assertTrue(assembler.accept(PEER, UdpProtocol.decode(packets.get(1))).isPresent());
    }

    @Test
    void rejectsForeignClassesWrongTypesAndOversizedMessages() throws Exception {
        assertThrows(IOException.class, () -> SerializeUtil.deserialize(
                SerializeUtil.serialize(new HashMap<>()), CommandRequest.class));
        assertThrows(IOException.class, () -> SerializeUtil.deserialize(
                SerializeUtil.serialize(CommandResponse.ok("x")), CommandRequest.class));
        assertThrows(IOException.class, () -> SerializeUtil.serialize("x".repeat(UdpProtocol.MAX_MESSAGE_BYTES)));
        assertThrows(IOException.class, () -> UdpProtocol.decode(new byte[100]));
    }

    @Test
    void deserializationRechecksConstructorInvariants() throws Exception {
        var coordinates = new Coordinates(1f, 2L);
        var x = Coordinates.class.getDeclaredField("x");
        x.setAccessible(true);
        x.set(coordinates, Float.NaN);
        assertThrows(IOException.class, () -> SerializeUtil.deserialize(
                SerializeUtil.serialize(coordinates), Coordinates.class));
    }
}
