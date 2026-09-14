package service;

import db.PersistenceException;
import models.Route;
import models.RouteData;
import repository.RouteRepository;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Единственный владелец коллекции. Монитор synchronized-обёртки защищает также
 * индекс byId и составную операцию «проверка -> commit -> изменение памяти».
 */
public final class CollectionService {
    private final NavigableSet<Route> routes = Collections.synchronizedNavigableSet(new TreeSet<>());
    private final Map<Integer, Route> byId = new HashMap<>();
    private final RouteRepository repository;
    private final Instant initializedAt = Instant.now();
    private boolean available = true;

    public CollectionService(RouteRepository repository) {
        this.repository = repository;
        for (Route route : repository.loadAll()) remember(route);
    }

    public List<Route> snapshot() {
        synchronized (routes) {
            requireAvailable();
            return List.copyOf(routes);
        }
    }

    public String info() {
        synchronized (routes) {
            requireAvailable();
            return "Тип: TreeSet (Collections.synchronizedNavigableSet)"
                    + "\nИнициализация: " + initializedAt + "\nЭлементов: " + routes.size();
        }
    }

    public Optional<Route> add(RouteData data, int ownerId, boolean onlyIfMin) {
        return write(() -> {
            if (onlyIfMin && !routes.isEmpty()
                    && Route.DISTANCE_ORDER.compare(data.getDistance(), routes.first().getDistance()) >= 0) {
                return Optional.empty();
            }
            Route saved = repository.insert(data, ownerId);
            remember(saved); // insert вернулся только после commit.
            return Optional.of(saved);
        });
    }

    public Route update(int id, RouteData data, int ownerId) {
        return write(() -> {
            Route old = owned(id, ownerId);
            Route saved = repository.update(id, data, ownerId);
            routes.remove(old);
            byId.remove(id);
            remember(saved);
            return saved;
        });
    }

    public int removeById(int id, int ownerId) {
        return write(() -> deleteSelected(List.of(owned(id, ownerId)), ownerId));
    }

    public int clear(int ownerId) { return removeMatching(ownerId, route -> true); }

    public int removeCompared(RouteData reference, int ownerId, boolean greater) {
        return removeMatching(ownerId, route -> {
            int comparison = Route.DISTANCE_ORDER.compare(route.getDistance(), reference.getDistance());
            return greater ? comparison > 0 : comparison < 0;
        });
    }

    public int removeAnyByDistance(Long distance, int ownerId) {
        return write(() -> {
            List<Route> selected = routes.stream()
                    .filter(route -> route.getOwnerId() == ownerId && Objects.equals(route.getDistance(), distance))
                    .limit(1).toList();
            return deleteSelected(selected, ownerId);
        });
    }

    private int removeMatching(int ownerId, Predicate<Route> condition) {
        return write(() -> deleteSelected(routes.stream()
                .filter(route -> route.getOwnerId() == ownerId && condition.test(route)).toList(), ownerId));
    }

    private int deleteSelected(List<Route> selected, int ownerId) {
        if (selected.isEmpty()) return 0;
        repository.delete(selected.stream().map(Route::getId).toList(), ownerId);
        for (Route route : selected) {
            routes.remove(route);
            byId.remove(route.getId());
        }
        return selected.size();
    }

    private Route owned(int id, int ownerId) {
        Route route = byId.get(id);
        if (route == null) throw new IllegalArgumentException("Маршрут с id=" + id + " не найден.");
        if (route.getOwnerId() != ownerId) throw new IllegalArgumentException("Можно изменять только свои маршруты.");
        return route;
    }

    private void remember(Route route) {
        if (byId.containsKey(route.getId())) {
            available = false;
            throw new IllegalStateException("Повторный ID в коллекции; требуется перезапуск.");
        }
        byId.put(route.getId(), route);
        routes.add(route);
    }

    private <T> T write(Supplier<T> operation) {
        synchronized (routes) {
            requireAvailable();
            try {
                return operation.get();
            } catch (PersistenceException e) {
                if (e.isUncertain()) available = false;
                throw e;
            }
        }
    }

    private void requireAvailable() {
        if (!available) {
            throw new IllegalStateException("Состояние БД после сбоя неизвестно. Перезапустите сервер для загрузки коллекции.");
        }
    }
}
