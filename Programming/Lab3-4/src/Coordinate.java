import java.util.*;
public record Coordinate(double x, double y) {
    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
