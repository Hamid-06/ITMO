import java.util.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Симуляция начинается ===\n");


        Environment environment = Environment.randomEnvironment(10);
        System.out.println(environment);


        Agent agent = new Agent("Исследователь");


        WaterTransport boat = new WaterTransport(
                "Примитивное средство",
                new Coordinate(0, 0),
                1.0,
                0.2
        );


        agent.addTransport(boat);
        agent.setEnvironment(environment);
        agent.setTargetDistance(40.0);


        try {
            agent.act();
        } catch (ActionException e) {
            System.out.println(e.getMessage());
        }


        System.out.println("\n=== Симуляция завершена ===");

    }
}