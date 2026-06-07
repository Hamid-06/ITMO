import java.util.*;
abstract public class AbstractEntity {
    protected final String name;
    protected Coordinate coordinate;


    protected AbstractEntity(String name, Coordinate coordinate) {
        this.name = Objects.requireNonNull(name);
        this.coordinate = Objects.requireNonNull(coordinate);
    }


    public abstract String describe();


    @Override
    public String toString() {
        return describe();
    }

}

