package support;

import db.PersistenceException;
import models.Route;
import models.RouteData;
import repository.RouteRepository;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Только тестовый дублёр. Производственный сервер всегда использует PostgreSQL. */
public class MemoryRouteRepository implements RouteRepository {
    public final Map<Integer, Route> rows = new HashMap<>();
    public final AtomicInteger loads = new AtomicInteger();
    public boolean fail;
    public boolean uncertain;
    private int nextId = 1;

    @Override
    public List<Route> loadAll() {
        loads.incrementAndGet();
        return new ArrayList<>(rows.values());
    }

    @Override
    public Route insert(RouteData data, int ownerId) {
        checkFailure();
        Route route = make(nextId++, data, new Date(), ownerId);
        rows.put(route.getId(), route);
        return route;
    }

    @Override
    public Route update(int id, RouteData data, int ownerId) {
        checkFailure();
        Route old = rows.get(id);
        if (old == null || old.getOwnerId() != ownerId) throw new AssertionError("Owner guard failed");
        Route route = make(id, data, old.getCreationDate(), ownerId);
        rows.put(id, route);
        return route;
    }

    @Override
    public void delete(List<Integer> ids, int ownerId) {
        checkFailure();
        for (int id : ids) {
            if (rows.get(id).getOwnerId() != ownerId) throw new AssertionError("Owner guard failed");
        }
        ids.forEach(rows::remove);
    }

    private void checkFailure() {
        if (fail) throw new PersistenceException("Injected database failure", null, uncertain);
    }

    public static Route make(int id, RouteData data, Date date, int ownerId) {
        return new Route(id, data.getName(), data.getCoordinates(), date,
                data.getFrom(), data.getTo(), data.getDistance(), ownerId);
    }
}
