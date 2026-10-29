package models;

import java.io.Serial;
import java.io.Serializable;

/** Данные, которые вводит клиент. ID, дату и владельца назначает сервер. */
public final class RouteData implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final String name;
    private final Coordinates coordinates;
    private final Location from;
    private final Location to;
    private final Long distance;

    public RouteData(String name, Coordinates coordinates, Location from, Location to, Long distance) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Имя не может быть пустым.");
        if (coordinates == null) throw new IllegalArgumentException("Координаты обязательны.");
        if (from == null) throw new IllegalArgumentException("Начальная точка обязательна.");
        if (distance != null && distance <= 1) throw new IllegalArgumentException("Дистанция должна быть > 1.");
        this.name = name;
        this.coordinates = coordinates;
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public Location getFrom() { return from; }
    public Location getTo() { return to; }
    public Long getDistance() { return distance; }

    @Serial
    private Object readResolve() { return new RouteData(name, coordinates, from, to, distance); }
}
