package models;

public class Location {
        private final long x;
        private final Double y;
        private final Long z;

        public Location(long x, Double y ,Long z){
            if ( y == null ) throw new IllegalArgumentException("Location.y  может быть null");
            if ( z == null ) throw new IllegalArgumentException("Location.z  может быть null");
            this.x = x;
            this.y = y;
            this.z = z;

        }

    public Long getZ() {
        return z;
    }

    public long getX() {
        return x;
    }

    public Double getY() {
        return y;
    }
    @Override
    public String toString() {
        return "Location{" + "x=" + x + ", y=" + y + ", name='" + z + '\'' + '}';
    }
}

