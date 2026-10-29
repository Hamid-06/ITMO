package models;

import java.io.Serializable;
import java.util.Objects;

public class Location implements Serializable {
    private static final long serialVersionUID = 1L;

    private final long x;
    private final Double y;
    private final Long z;

    public Location(long x, Double y, Long z) {
        if (y == null) throw new IllegalArgumentException("location.y cannot be null");
        if (z == null) throw new IllegalArgumentException("location.z cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long   getX() { return x; }
    public Double getY() { return y; }
    public Long   getZ() { return z; }

    @Override
    public String toString() { return "Location{x=" + x + ", y=" + y + ", z=" + z + "}"; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Location)) return false;
        Location l = (Location) o;
        return x == l.x && Objects.equals(y, l.y) && Objects.equals(z, l.z);
    }

    @Override
    public int hashCode() { return Objects.hash(x, y, z); }
}