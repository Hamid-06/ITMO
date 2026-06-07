package models;

import java.util.Date;

public class Route implements Comparable<Route> {

    private final Integer id;
    private final String name;
    private final Coordinates coordinates;
    private final Date creationDate;
    private final Location from;
    private final Location to;
    private final Long distance;


    public Route(Integer id, String name, Coordinates coordinates,
                 Date creationDate, Location from, Location to, Long distance){
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
        this.creationDate = creationDate;
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    public Integer getId(){ return id; }
    public String getName(){ return name; }
    public Coordinates getCoordinates(){ return coordinates; }
    public Date getCreationDate(){ return creationDate; }
    public Location getFrom(){ return from; }
    public Location getTo(){ return to; }
    public Long getDistance(){ return distance; }

    @Override
    public int compareTo(Route o) {
        if (this.distance == null && o.distance == null) return 0;
        if (this.distance == null) return -1;
        if (o.distance == null) return 1;
        return Long.compare(this.distance, o.distance);
    }
    @Override
    public String toString() {
        return "Route{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", from=" + from +
                ", to=" + to +
                ", distance=" + distance +
                '}';
    }
}