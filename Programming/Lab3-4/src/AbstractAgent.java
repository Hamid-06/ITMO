import java.util.*;
abstract public class AbstractAgent extends AbstractEntity implements Strategist {
    protected StrategyType currentStrategy;


    protected AbstractAgent(String name) {
        super(name, new Coordinate(0, 0));
    }


    public abstract void act() throws ActionException;
}

