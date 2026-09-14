package network;

import models.Coordinates;
import models.Location;
import models.Route;
import models.RouteData;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/** Принимаем только классы протокола, а не произвольные Java-объекты. */
public final class SerializeUtil {
    private static final Set<Class<?>> ALLOWED = Set.of(
            CommandRequest.class, CommandResponse.class, CommandResponse.Status.class,
            CommandType.class, Credentials.class, Route.class, RouteData.class,
            Coordinates.class, Location.class, String.class, Integer.class, Long.class,
            Float.class, Double.class, Number.class, Enum.class, UUID.class,
            Date.class, ArrayList.class, Object.class);

    private SerializeUtil() { }

    public static byte[] serialize(Serializable object) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(new FilterOutputStream(bytes) {
            private int written;
            private void reserve(int count) throws IOException {
                if (count > UdpProtocol.MAX_MESSAGE_BYTES - written) {
                    throw new IOException("Сообщение превышает лимит 2 МиБ. Используйте фильтр.");
                }
                written += count;
            }
            @Override public void write(int value) throws IOException { reserve(1); out.write(value); }
            @Override public void write(byte[] data, int offset, int length) throws IOException {
                reserve(length);
                out.write(data, offset, length);
            }
        })) {
            output.writeObject(object);
        }
        return bytes.toByteArray();
    }

    public static <T> T deserialize(byte[] bytes, Class<T> expected) throws IOException {
        if (bytes.length > UdpProtocol.MAX_MESSAGE_BYTES) throw new IOException("Слишком большое сообщение.");
        try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            input.setObjectInputFilter(info -> {
                if (info.depth() > 32 || info.references() > 200_000
                        || info.streamBytes() > UdpProtocol.MAX_MESSAGE_BYTES
                        || info.arrayLength() > UdpProtocol.MAX_MESSAGE_BYTES) {
                    return ObjectInputFilter.Status.REJECTED;
                }
                Class<?> type = info.serialClass();
                if (type == null) return ObjectInputFilter.Status.UNDECIDED;
                while (type.isArray()) type = type.getComponentType();
                return type.isPrimitive() || ALLOWED.contains(type)
                        ? ObjectInputFilter.Status.ALLOWED : ObjectInputFilter.Status.REJECTED;
            });
            Object result = input.readObject();
            if (!expected.isInstance(result)) throw new IOException("Неверный тип сообщения.");
            if (input.read() != -1) throw new IOException("Лишние данные после сообщения.");
            return expected.cast(result);
        } catch (ClassNotFoundException | IllegalArgumentException | NullPointerException e) {
            throw new IOException("Некорректные данные сообщения.", e);
        }
    }
}
