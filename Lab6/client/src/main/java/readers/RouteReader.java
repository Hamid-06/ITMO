package readers;

import io.InputHandler;
import models.Coordinates;
import models.Location;
import models.Route;

import java.util.Date;

public class RouteReader {

    private final InputHandler input;

    public RouteReader(InputHandler input) {
        this.input = input;
    }

    public Route read() {
        input.getConsole().println("--- New Route ---");

        String name = input.readNonEmpty("Name");

        input.getConsole().println("  [Coordinates]");
        Float cx = input.readFloatGreaterThan("  x", -176f);
        Long  cy = input.readLong("  y");
        Coordinates coordinates = new Coordinates(cx, cy);

        input.getConsole().println("  [From location]");
        Location from = readLocation();

        Location to = null;
        if (input.readConfirm("Specify arrival location (to)?")) {
            input.getConsole().println("  [To location]");
            to = readLocation();
        }

        Long distance = input.readLongNullableGreaterThan("Distance", 1L);

        input.getConsole().println("-----------------");

        // id=1 is a placeholder; server assigns real id via IdGenerator
        return new Route(1, name, coordinates, new Date(), from, to, distance);
    }

    private Location readLocation() {
        long   x = input.readPrimitiveLong("  x");
        Double y = input.readDouble("  y");
        Long   z = input.readLong("  z");
        return new Location(x, y, z);
    }
}