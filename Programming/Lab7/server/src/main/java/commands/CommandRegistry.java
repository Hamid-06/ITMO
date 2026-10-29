package commands;

import models.Coordinates;
import models.Route;
import network.CommandRequest;
import network.CommandResponse;
import network.CommandType;
import service.CollectionService;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Command: таблица обработчиков вместо повторяющихся ветвей сетевого сервера. */
public final class CommandRegistry {
    private final Map<CommandType, ServerCommand> commands = new EnumMap<>(CommandType.class);

    public CommandRegistry(CollectionService collection) {
        commands.put(CommandType.HELP, (request, user) -> CommandResponse.ok(CommandType.help()));
        commands.put(CommandType.INFO, (request, user) -> CommandResponse.ok(collection.info()));
        commands.put(CommandType.SHOW, (request, user) ->
                CommandResponse.ok("Коллекция:", collection.snapshot()));
        commands.put(CommandType.FILTER_CONTAINS_NAME, (request, user) ->
                CommandResponse.ok("Найденные маршруты:", collection.snapshot().stream()
                        .filter(route -> route.getName().contains(request.getStringArg())).toList()));
        commands.put(CommandType.GROUP_COUNTING_BY_COORDINATES, (request, user) -> {
            Map<Coordinates, Long> counts = collection.snapshot().stream().collect(Collectors.groupingBy(
                    Route::getCoordinates, LinkedHashMap::new, Collectors.counting()));
            String text = counts.entrySet().stream().map(e -> e.getKey() + " -> " + e.getValue())
                    .collect(Collectors.joining("\n"));
            return CommandResponse.ok(counts.isEmpty() ? "Коллекция пуста." : text);
        });
        commands.put(CommandType.ADD, add(collection, false));
        commands.put(CommandType.ADD_IF_MIN, add(collection, true));
        commands.put(CommandType.UPDATE, (request, user) -> {
            Route saved = collection.update(id(request), request.getRouteArg(), user);
            return CommandResponse.ok("Маршрут " + saved.getId() + " обновлён.");
        });
        commands.put(CommandType.REMOVE_BY_ID, (request, user) ->
                removed(collection.removeById(id(request), user)));
        commands.put(CommandType.CLEAR, (request, user) -> removed(collection.clear(user)));
        commands.put(CommandType.REMOVE_GREATER, (request, user) ->
                removed(collection.removeCompared(request.getRouteArg(), user, true)));
        commands.put(CommandType.REMOVE_LOWER, (request, user) ->
                removed(collection.removeCompared(request.getRouteArg(), user, false)));
        commands.put(CommandType.REMOVE_ANY_BY_DISTANCE, (request, user) ->
                removed(collection.removeAnyByDistance(request.getLongArg(), user)));
    }

    public CommandResponse execute(CommandRequest request, int userId) {
        validate(request);
        ServerCommand command = commands.get(request.getType());
        if (command == null) throw new IllegalArgumentException("Эта команда выполняется на клиенте.");
        return command.execute(request, userId);
    }

    private static ServerCommand add(CollectionService collection, boolean onlyIfMin) {
        return (request, user) -> collection.add(request.getRouteArg(), user, onlyIfMin)
                .map(route -> CommandResponse.ok("Маршрут добавлен. id=" + route.getId()))
                .orElseGet(() -> CommandResponse.ok("Маршрут не добавлен: дистанция не меньше минимума."));
    }

    private static CommandResponse removed(int count) {
        return CommandResponse.ok("Удалено своих маршрутов: " + count);
    }

    private static void validate(CommandRequest request) {
        switch (request.getType().arguments()) {
            case ROUTE, ID_ROUTE -> {
                if (request.getRouteArg() == null) throw new IllegalArgumentException("Нужны данные маршрута.");
                if (request.getType().arguments() == CommandType.Arguments.ID_ROUTE) id(request);
            }
            case ID -> id(request);
            case TEXT -> {
                if (request.getStringArg() == null || request.getStringArg().isBlank()) {
                    throw new IllegalArgumentException("Нужна непустая подстрока.");
                }
            }
            case DISTANCE -> {
                if (request.getLongArg() != null && request.getLongArg() <= 1) {
                    throw new IllegalArgumentException("Дистанция должна быть > 1 или null.");
                }
            }
            default -> { }
        }
    }

    private static int id(CommandRequest request) {
        Long id = request.getLongArg();
        if (id == null || id <= 0 || id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("ID должен быть целым числом от 1 до " + Integer.MAX_VALUE + ".");
        }
        return id.intValue();
    }
}
