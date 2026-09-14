package repository;

import models.Route;
import models.RouteData;
import java.util.List;

/** Все методы записи возвращаются только после успешного commit. */
public interface RouteRepository {
    List<Route> loadAll();
    Route insert(RouteData data, int ownerId);
    Route update(int id, RouteData data, int ownerId);
    void delete(List<Integer> ids, int ownerId);
}
