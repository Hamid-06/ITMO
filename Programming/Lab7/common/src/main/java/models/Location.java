package models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class Location implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final long x;
    private final Double y;
    private final Long z;

    public Location(long x, Double y, Long z) {
        if (y == null) throw new IllegalArgumentException("location.y обязателен.");
        if (z == null) throw new IllegalArgumentException("location.z обязателен.");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getX() { return x; }
    public Double getY() { return y; }
    public Long getZ() { return z; }

    @Serial
    private Object readResolve() { return new Location(x, y, z); }

    @Override
    public boolean equals(Object other) {
        return other instanceof Location l && x == l.x && Objects.equals(y, l.y) && Objects.equals(z, l.z);
    }

    @Override
    public int hashCode() { return Objects.hash(x, y, z); }

    @Override
    public String toString() { return "Location{x=" + x + ", y=" + y + ", z=" + z + "}"; }
}
