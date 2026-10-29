package managers;

import models.Route;

import javax.xml.stream.Location;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.TreeSet;

public class CollectionManager {

    private final TreeSet<Route> collection = new TreeSet<>();
    private final LocalDateTime initDate = LocalDateTime.now();
    private final IdGenerator idGenerator = new IdGenerator();


    public void add(Route route){

        if (route == null) throw new IllegalArgumentException("route cannot be null");

        int newId = idGenerator.generateId();
        Date now =new Date();

        Route withId = new Route(
                newId,
                route.getName(),
                route.getCoordinates(),
                now,
                route.getFrom(),
                route.getTo(),
                route.getDistance()
        );

            collection.add(withId);

    }

    public void loadFileFrom(Route route){
        if (route == null) return;
        idGenerator.markUsed(route.getId());
        collection.add(route);
    }

    public boolean update(int id, Route newRoute){
        Optional<Route> existing = collection.stream().filter(r -> r.getId() ==id ).findFirst();

        if (existing.isEmpty()) return false;
        Date originalDate = existing.get().getCreationDate();
        collection.remove(existing.get());

        Route updated = new Route(
                id,
                newRoute.getName(),
                newRoute.getCoordinates(),
                originalDate,
                newRoute.getFrom(),
                newRoute.getTo(),
                newRoute.getDistance()
        );
        collection.add(updated);
        return true;
    }
    public boolean removeById(int id){
        boolean removed = collection.removeIf(r -> r.getId()==id);
        if (removed) idGenerator.release(id);
        return removed;
    }

    public void clear(){
        collection.clear();
        idGenerator.reset();
    }

    public void addIfMin(Route route){
        if (collection.isEmpty() || route.compareTo(collection.first()) < 0) {
            add(route);
        }else {
            System.out.println("Элемент не меньше минимального — не добавлен.");
        }
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


    public List<Route> filterByName(String substring) {
        if (substring == null) return Collections.emptyList();
        return collection.stream()
                .filter(r -> r.getName().contains(substring))
                .collect(Collectors.toList());
    }


    public Map<String, Long> groupByCoordinates() {
        return collection.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCoordinates().toString(),
                        Collectors.counting()
                ));
    }


    public String getInfo() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        return "Тип коллекции : " + collection.getClass().getName() + "\n" +
                "Дата инициализации: " + initDate.format(fmt) + "\n" +
                "Количество элементов: " + collection.size();
    }


    public TreeSet<Route> getAll() {
        return collection;
    }


    public boolean isEmpty() {
        return collection.isEmpty();
    }


    public int size() {
        return collection.size();
    }


    public Optional<Route> findById(int id) {
        return collection.stream()
                .filter(r -> r.getId() == id)
                .findFirst();
    }
}
