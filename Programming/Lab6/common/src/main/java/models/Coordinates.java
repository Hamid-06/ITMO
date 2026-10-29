package models;

import java.io.Serializable;
import java.util.Objects;

public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Float x;
    private final Long y;

    public Coordinates(Float x, Long y) {
        if (x == null || x <= -176f)
            throw new IllegalArgumentException("coordinates.x must be > -176");
        if (y == null)
            throw new IllegalArgumentException("coordinates.y cannot be null");
        this.x = x;
        this.y = y;
    }

    public Float getX() { return x; }
    public Long  getY() { return y; }

    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Coordinates)) return false;
        Coordinates c = (Coordinates) o;
        return Objects.equals(x, c.x) && Objects.equals(y, c.y);
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }
}