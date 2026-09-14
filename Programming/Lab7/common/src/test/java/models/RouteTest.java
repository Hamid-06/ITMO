package models;

import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.TreeSet;
import static org.junit.jupiter.api.Assertions.*;

class RouteTest {
    private Route route(int id, Long distance, Date date) {
        return new Route(id, "route", new Coordinates(0f, 1L), date, new Location(1, 2.0, 3L), null, distance, 1);
    }

    @Test
    void preservesSameDistanceObjectsAndSortsNullFirst() {
        TreeSet<Route> routes = new TreeSet<>();
        routes.add(route(1, 8L, new Date()));
        routes.add(route(2, 8L, new Date()));
        routes.add(route(3, null, new Date()));
        assertEquals(3, routes.size());
        assertEquals(3, routes.first().getId());
        assertEquals(2, routes.last().getId());
        assertEquals(route(1, 8L, new Date()), route(1, 8L, new Date()));
        assertNotEquals(route(1, 8L, new Date()), route(1, 9L, new Date()));
    }

    @Test
    void dateIsDefensivelyCopied() {
        Date date = new Date(1000);
        Route route = route(1, 2L, date);
        date.setTime(0);
        route.getCreationDate().setTime(0);
        assertEquals(1000, route.getCreationDate().getTime());
    }
}
