package managers;

import models.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionManager {

    private final TreeSet<Route> collection = new TreeSet<>();
    private final LocalDateTime initDate    = LocalDateTime.now();
    private final IdGenerator idGenerator   = new IdGenerator();

    // ── Basic CRUD ─────────────────────────────────────────────────────────

    public void add(Route route) {
        int newId = idGenerator.generateId();
        Route withId = new Route(newId, route.getName(), route.getCoordinates(),
                new Date(), route.getFrom(), route.getTo(), route.getDistance());
        collection.add(withId);
    }

    public void loadFromFile(Route route) {
        if (route == null) return;
        idGenerator.markUsed(route.getId());
        collection.add(route);
    }

    public boolean update(int id, Route newData) {
        Optional<Route> existing = findById(id);
        if (existing.isEmpty()) return false;
        Date originalDate = existing.get().getCreationDate();
        collection.remove(existing.get());
        Route updated = new Route(id, newData.getName(), newData.getCoordinates(),
                originalDate, newData.getFrom(), newData.getTo(), newData.getDistance());
        collection.add(updated);
        return true;
    }

    public boolean removeById(int id) {
        boolean removed = collection.removeIf(r -> r.getId() == id);
        if (removed) idGenerator.release(id);
        return removed;
    }

    public void clear() {
        collection.clear();
        idGenerator.reset();
    }

    public Optional<Route> findById(int id) {
        return collection.stream()
                .filter(r -> r.getId() == id)
                .findFirst();
    }

    // ── Comparison commands ────────────────────────────────────────────────

    public void addIfMin(Route route) {
        boolean isMin = collection.isEmpty() ||
                collection.stream().allMatch(r -> route.compareTo(r) < 0);
        if (isMin) add(route);
    }

    public int removeGreater(Route route) {
        List<Route> toRemove = collection.stream()
                .filter(r -> r.compareTo(route) > 0)
                .collect(Collectors.toList());
        toRemove.forEach(r -> {
            collection.remove(r);
            idGenerator.release(r.getId());
        });
        return toRemove.size();
    }

    public int removeLower(Route route) {
        List<Route> toRemove = collection.stream()
                .filter(r -> r.compareTo(route) < 0)
                .collect(Collectors.toList());
        toRemove.forEach(r -> {
            collection.remove(r);
            idGenerator.release(r.getId());
        });
        return toRemove.size();
    }

    public boolean removeAnyByDistance(Long distance) {
        Optional<Route> found = collection.stream()
                .filter(r -> Objects.equals(r.getDistance(), distance))
                .findFirst();
        found.ifPresent(r -> {
            collection.remove(r);
            idGenerator.release(r.getId());
        });
        return found.isPresent();
    }

    // ── Filter / Group ─────────────────────────────────────────────────────

    public List<Route> filterByName(String substring) {
        if (substring == null) return Collections.emptyList();
        return collection.stream()
                .filter(r -> r.getName().contains(substring))
                .sorted()
                .collect(Collectors.toList());
    }

    public Map<String, Long> groupByCoordinates() {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCoordinates().toString(),
                        Collectors.counting()));
    }

    public List<Route> getAllSorted() {
        return collection.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    // ── Info ───────────────────────────────────────────────────────────────

    public String getInfo() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return "Type       : " + collection.getClass().getName() + "\n" +
                "Init date  : " + initDate.format(fmt) + "\n" +
                "Size       : " + collection.size();
    }

    public TreeSet<Route> getAll()  { return collection; }
    public boolean        isEmpty() { return collection.isEmpty(); }
    public int            size()    { return collection.size(); }
}