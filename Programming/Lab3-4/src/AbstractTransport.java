import java.util.*;
public abstract class AbstractTransport extends AbstractEntity implements Movable{
    protected double capacity;
    protected double efficiency;


    protected AbstractTransport(String name, Coordinate coordinate, double capacity, double efficiency) {
        super(name, coordinate);
        this.capacity = capacity;
        this.efficiency = efficiency;
    }


    public abstract boolean canReach(double distance);
}
