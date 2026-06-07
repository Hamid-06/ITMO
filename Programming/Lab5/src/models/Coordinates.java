package models;

public class Coordinates {
    private final Float x;
    private final Long y;

    public Coordinates(Float x, Long y){
       if (x <= -176) throw new IllegalArgumentException("Coordinates.x должно быть больше -176");
       if (x == null) throw new IllegalArgumentException("Coordinates.x не может быть null");
       if (y == null) throw new IllegalArgumentException("Coordinates.y не может быть null");
        this.x = x ;
        this.y = y;
    }

    public Float getX() {
        return x;
    }

    public Long getY() {
        return y;
    }
    @Override
    public String toString() {
        return "Coordinates{" + "x=" + x + ", y=" + y + '}';
    }
}
