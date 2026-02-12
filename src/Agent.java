import java.util.*;

public class Agent extends AbstractAgent {
    private final List<AbstractTransport> transports = new ArrayList<>();
    private Environment environment;
    private double targetDistance;
    private final Random random = new Random();
    public Agent(String name) {
        super(name);
    }
    public void addTransport(AbstractTransport transport) {
        transports.add(transport);
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
    public void setTargetDistance(double targetDistance) {
        this.targetDistance = targetDistance;
    }
    @Override
    public StrategyType chooseStrategy() {
        if (random.nextBoolean()) {
            return StrategyType.ESCAPE;
        }
        return StrategyType.PERIMETER_EXPLORATION;
    }
    @Override
    public void act() throws ActionException {
        if (environment == null) {
            throw new IllegalStateException("Среда не установлена.");
        }
        currentStrategy = chooseStrategy();
        System.out.println("Агент выбирает стратегию: " + currentStrategy);
        if (transports.isEmpty()) {
            throw new ActionException("Нет доступного транспорта.");
        }
        AbstractTransport transport = transports.get(0);
        switch (currentStrategy) {
            case ESCAPE -> attemptEscape(transport);
            case PERIMETER_EXPLORATION -> explorePerimeter(transport);
        }
    }
    private void attemptEscape(AbstractTransport transport) throws ActionException {
        System.out.println("Попытка достичь внешней среды...");


        if (!transport.canReach(targetDistance)) {
            System.out.println("Транспорт недостаточен для побега.");
            improveTransport(transport);
            throw new ActionException("Недостаточная эффективность транспорта для побега.");
        }


        try {
            transport.move(new Coordinate(targetDistance, 0));
            System.out.println("Агент достиг внешней среды.");
        } catch (MovementException e) {
            throw new ActionException(e.getMessage());
        }
    }
    private void explorePerimeter(AbstractTransport transport) {
        System.out.println("Агент исследует границу системы...");
        for (Coordinate point : environment.getBoundaryPoints()) {
            try {
                transport.move(point);
            } catch (MovementException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    private void improveTransport(AbstractTransport transport) {
        if (transport instanceof Modifiable modifiable) {
            System.out.println("Агент модифицирует транспорт.");
            modifiable.modify(new Module("Усиление", 0.5));
        }
    }
    @Override
    public String describe() {
        return "Agent{name='" + name + "'}";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent agent)) return false;
        return Objects.equals(name, agent.name);
    }
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}