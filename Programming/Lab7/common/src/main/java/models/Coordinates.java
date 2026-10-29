package models;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public final class Coordinates implements Serializable {
    @Serial private static final long serialVersionUID = 1L;
    private final Float x;
    private final Long y;

    public Coordinates(Float x, Long y) {
        if (x == null || !(x > -176f)) {
            throw new IllegalArgumentException("coordinates.x должен быть > -176 и не NaN.");
        }
        if (y == null) throw new IllegalArgumentException("coordinates.y обязателен.");
        this.x = x;
        this.y = y;
    }

    public Float getX() { return x; }
    public Long getY() { return y; }

    @Serial
    private Object readResolve() { return new Coordinates(x, y); }

    @Override
    public boolean equals(Object other) {
        return other instanceof Coordinates c && Objects.equals(x, c.x) && Objects.equals(y, c.y);
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }
}
