package models;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Route implements Serializable, Comparable<Route> {
    private static final long serialVersionUID = 1L;

    private final Integer id;
    private final String name;
    private final Coordinates coordinates;
    private final Date creationDate;
    private final Location from;
    private final Location to;
    private final Long distance;

    public Route(Integer id, String name, Coordinates coordinates,
                 Date creationDate, Location from, Location to, Long distance) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("id must be > 0");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name cannot be empty");
        if (coordinates == null)
            throw new IllegalArgumentException("coordinates cannot be null");
        if (creationDate == null)
            throw new IllegalArgumentException("creationDate cannot be null");
        if (from == null)
            throw new IllegalArgumentException("from cannot be null");
        if (distance != null && distance <= 1)
            throw new IllegalArgumentException("distance must be > 1");

        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = new Date(creationDate.getTime());
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public Integer getId()              { return id; }
    public String getName()             { return name; }
    public Coordinates getCoordinates() { return coordinates; }
    public Date getCreationDate()       { return new Date(creationDate.getTime()); }
    public Location getFrom()           { return from; }
    public Location getTo()             { return to; }
    public Long getDistance()           { return distance; }

    @Override
    public int compareTo(Route o) {
        if (this.distance == null && o.distance == null) return Integer.compare(this.id, o.id);
        if (this.distance == null) return -1;
        if (o.distance == null)   return 1;
        int cmp = Long.compare(this.distance, o.distance);
        if (cmp != 0) return cmp;
        return Integer.compare(this.id, o.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Route)) return false;
        return Objects.equals(id, ((Route) obj).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", from=" + from +
                ", to=" + (to != null ? to : "null") +
                ", distance=" + (distance != null ? distance : "null") +
                '}';
    }
}