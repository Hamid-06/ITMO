package client;

import models.Coordinates;
import models.Location;
import models.RouteData;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Function;

/** В консоли повторяем ошибочное поле; в скрипте останавливаем текущий файл. */
public final class RouteReader {
    private final InputStack input;
    private final PrintWriter output;

    public RouteReader(InputStack input, PrintWriter output) {
        this.input = input;
        this.output = output;
    }

    public RouteData read() throws IOException {
        String name = field("Название: ", value -> {
            if (value.isBlank()) throw new IllegalArgumentException("Название не должно быть пустым.");
            return value;
        });
        Float x = field("coordinates.x (> -176): ", value -> {
            float number = Float.parseFloat(value.strip());
            if (!(number > -176f)) throw new IllegalArgumentException("x должен быть > -176, NaN запрещён.");
            return number;
        });
        Long y = field("coordinates.y (целое): ", value -> Long.valueOf(value.strip()));
        Location from = location("from");
        boolean hasTo = field("Указать to? [y/n]: ", value -> switch (value.strip().toLowerCase(java.util.Locale.ROOT)) {
            case "y", "yes", "да" -> true;
            case "n", "no", "нет" -> false;
            default -> throw new IllegalArgumentException("Введите y или n.");
        });
        Location to = hasTo ? location("to") : null;
        Long distance = field("distance (> 1; пусто = null): ", value -> {
            if (value.isBlank() || value.strip().equalsIgnoreCase("null")) return null;
            long number = Long.parseLong(value.strip());
            if (number <= 1) throw new IllegalArgumentException("Дистанция должна быть > 1.");
            return number;
        });
        return new RouteData(name, new Coordinates(x, y), from, to, distance);
    }

    private Location location(String prefix) throws IOException {
        long x = field(prefix + ".x (целое): ", value -> Long.parseLong(value.strip()));
        Double y = field(prefix + ".y (вещественное): ", value -> Double.valueOf(value.strip()));
        Long z = field(prefix + ".z (целое): ", value -> Long.valueOf(value.strip()));
        return new Location(x, y, z);
    }

    private <T> T field(String prompt, Function<String, T> parser) throws IOException {
        while (true) {
            String line = input.valueLine(prompt, false);
            try {
                return parser.apply(line);
            } catch (IllegalArgumentException e) {
                String message = e instanceof NumberFormatException
                        ? "Ожидалось число подходящего типа и диапазона." : e.getMessage();
                if (input.inScript()) throw new IllegalArgumentException(prompt + message, e);
                output.println(message);
            }
        }
    }
}
