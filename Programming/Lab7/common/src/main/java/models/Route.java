package models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/** Неизменяемый снимок строки routes. При update создаётся новый экземпляр. */
public final class Route implements Serializable, Comparable<Route> {
    @Serial private static final long serialVersionUID = 2L;
    public static final Comparator<Long> DISTANCE_ORDER = Comparator.nullsFirst(Long::compareTo);

    private final Integer id;
    private final String name;
    private final Coordinates coordinates;
    private final Date creationDate;
    private final Location from;
    private final Location to;
    private final Long distance;
    private final Integer ownerId;

    public Route(Integer id, String name, Coordinates coordinates, Date creationDate,
                 Location from, Location to, Long distance, Integer ownerId) {
        if (id == null || id <= 0) throw new IllegalArgumentException("id должен быть > 0.");
        if (ownerId == null || ownerId <= 0) throw new IllegalArgumentException("ownerId должен быть > 0.");
        if (creationDate == null) throw new IllegalArgumentException("Дата создания обязательна.");
        new RouteData(name, coordinates, from, to, distance);
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = new Date(creationDate.getTime());
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.ownerId = ownerId;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public Date getCreationDate() { return new Date(creationDate.getTime()); }
    public Location getFrom() { return from; }
    public Location getTo() { return to; }
    public Long getDistance() { return distance; }
    public Integer getOwnerId() { return ownerId; }

    /** Совместимость с ранее написанным клиентским кодом. */
    public Integer getOwnerID() { return ownerId; }

    @Serial
    private Object readResolve() {
        return new Route(id, name, coordinates, creationDate, from, to, distance, ownerId);
    }

    @Override
    public int compareTo(Route other) {
        int byDistance = DISTANCE_ORDER.compare(distance, other.distance);
        return byDistance != 0 ? byDistance : Integer.compare(id, other.id);
    }

    // Равенство согласовано с естественным порядком TreeSet.
    // Уникальность ID отдельно обеспечивает PK в БД и CollectionService.
    @Override
    public boolean equals(Object other) {
        return other instanceof Route r && id.equals(r.id) && Objects.equals(distance, r.distance);
    }

    @Override
    public int hashCode() { return Objects.hash(id, distance); }

    @Override
    public String toString() {
        return "Route{id=" + id + ", name='" + name + "', coordinates=" + coordinates
                + ", creationDate=" + creationDate.toInstant() + ", from=" + from + ", to=" + to
                + ", distance=" + distance + ", ownerId=" + ownerId + "}";
    }
}
