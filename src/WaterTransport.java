import java.util.*;

public class WaterTransport extends AbstractTransport implements Modifiable{
    private final List<Module> modules = new ArrayList<>();


    public WaterTransport(String name, Coordinate coordinate, double capacity, double efficiency) {
        super(name, coordinate, capacity, efficiency);
    }


    @Override
    public boolean canReach(double distance) {
        double modifier = modules.stream()
                .mapToDouble(Module::powerModifier)
                .sum();
        double maxDistance = capacity * efficiency * (1 + modifier) * 100;
        return maxDistance >= distance;
    }


    @Override
    public void move(Coordinate coordinate) throws MovementException {
        if (efficiency <= 0) {
            throw new MovementException("Нулевая эффективность.");
        }
        this.coordinate = coordinate;
        System.out.println("Транспорт перемещён в " + coordinate);
    }


    @Override
    public void modify(Module module) {
        modules.add(module);
        System.out.println("Добавлен модуль: " + module);
    }


    @Override
    public String describe() {
        return "WaterTransport{name='" + name + "', coord=" + coordinate + "}";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WaterTransport that)) return false;
        return Objects.equals(name, that.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}


record Module(String name, double powerModifier) {}


class Environment {


    private final EnvironmentState state;
    private final List<Coordinate> boundaryPoints;


    public Environment(EnvironmentState state, List<Coordinate> boundaryPoints) {
        this.state = state;
        this.boundaryPoints = boundaryPoints;
    }


    public static Environment randomEnvironment(int boundarySize) {
        Random random = new Random();
        EnvironmentState state = EnvironmentState.values()[random.nextInt(EnvironmentState.values().length)];


        List<Coordinate> points = new ArrayList<>();
        for (int i = 0; i < boundarySize; i++) {
            points.add(new Coordinate(random.nextDouble() * 10, random.nextDouble() * 10));
        }


        return new Environment(state, points);
    }


    public List<Coordinate> getBoundaryPoints() {
        return boundaryPoints;
    }


    @Override
    public String toString() {
        return "Environment{state=" + state + ", boundaryPoints=" + boundaryPoints.size() + "}";
    }
}

