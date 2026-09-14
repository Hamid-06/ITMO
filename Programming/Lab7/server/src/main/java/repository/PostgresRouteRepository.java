package repository;

import db.Database;
import db.TableNames;
import models.Coordinates;
import models.Location;
import models.Route;
import models.RouteData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class PostgresRouteRepository implements RouteRepository {
    private static final String COLUMNS = """
            id, name, coordinates_x, coordinates_y, creation_date,
            from_x, from_y, from_z, to_x, to_y, to_z, distance, owner_id
            """;
    private final Database database;
    private final String table;

    public PostgresRouteRepository(Database database) { this(database, TableNames.DEFAULT); }

    public PostgresRouteRepository(Database database, TableNames names) {
        this.database = database;
        this.table = names.routes();
    }

    @Override
    public List<Route> loadAll() {
        return database.read(connection -> {
            try (PreparedStatement query = connection.prepareStatement("SELECT " + COLUMNS + " FROM " + table)) {
                query.setQueryTimeout(10);
                try (ResultSet rows = query.executeQuery()) {
                    List<Route> routes = new ArrayList<>();
                    while (rows.next()) routes.add(map(rows));
                    return routes;
                }
            }
        });
    }

    @Override
    public Route insert(RouteData data, int ownerId) {
        return database.transaction(connection -> {
            try (PreparedStatement query = connection.prepareStatement("""
                    INSERT INTO %s (name, coordinates_x, coordinates_y,
                        from_x, from_y, from_z, to_x, to_y, to_z, distance, owner_id)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING
                    """.formatted(table) + COLUMNS)) {
                bindData(query, data);
                query.setInt(11, ownerId);
                try (ResultSet rows = query.executeQuery()) {
                    if (!rows.next()) throw new SQLException("INSERT не вернул маршрут.");
                    return map(rows);
                }
            }
        });
    }

    @Override
    public Route update(int id, RouteData data, int ownerId) {
        return database.transaction(connection -> {
            try (PreparedStatement query = connection.prepareStatement("""
                    UPDATE %s SET name = ?, coordinates_x = ?, coordinates_y = ?,
                        from_x = ?, from_y = ?, from_z = ?, to_x = ?, to_y = ?, to_z = ?, distance = ?
                    WHERE id = ? AND owner_id = ? RETURNING
                    """.formatted(table) + COLUMNS)) {
                bindData(query, data);
                query.setInt(11, id);
                query.setInt(12, ownerId);
                try (ResultSet rows = query.executeQuery()) {
                    if (!rows.next()) throw new SQLException("Маршрут отсутствует или принадлежит другому пользователю.");
                    return map(rows);
                }
            }
        });
    }

    @Override
    public void delete(List<Integer> ids, int ownerId) {
        if (ids.isEmpty()) return;
        database.transaction(connection -> {
            try (PreparedStatement query = connection.prepareStatement(
                    "DELETE FROM " + table + " WHERE id = ? AND owner_id = ?")) {
                query.setQueryTimeout(10);
                for (int id : ids) {
                    query.setInt(1, id);
                    query.setInt(2, ownerId);
                    query.addBatch();
                }
                for (int count : query.executeBatch()) {
                    if (count != 1) throw new SQLException("Коллекция была изменена вне сервера; перезапустите сервер.");
                }
            }
            return null;
        });
    }

    private static void bindData(PreparedStatement query, RouteData data) throws SQLException {
        query.setQueryTimeout(10);
        query.setString(1, data.getName());
        query.setFloat(2, data.getCoordinates().getX());
        query.setLong(3, data.getCoordinates().getY());
        query.setLong(4, data.getFrom().getX());
        query.setDouble(5, data.getFrom().getY());
        query.setLong(6, data.getFrom().getZ());
        Location to = data.getTo();
        query.setObject(7, to == null ? null : to.getX(), Types.BIGINT);
        query.setObject(8, to == null ? null : to.getY(), Types.DOUBLE);
        query.setObject(9, to == null ? null : to.getZ(), Types.BIGINT);
        query.setObject(10, data.getDistance(), Types.BIGINT);
    }

    private static Route map(ResultSet rows) throws SQLException {
        Long toX = rows.getObject("to_x", Long.class);
        Double toY = rows.getObject("to_y", Double.class);
        Long toZ = rows.getObject("to_z", Long.class);
        if ((toX != null || toY != null || toZ != null) && (toX == null || toY == null || toZ == null)) {
            throw new SQLException("Неполная конечная точка маршрута.");
        }
        return new Route(rows.getInt("id"), rows.getString("name"),
                new Coordinates(rows.getFloat("coordinates_x"), rows.getLong("coordinates_y")),
                rows.getTimestamp("creation_date"),
                new Location(rows.getLong("from_x"), rows.getDouble("from_y"), rows.getLong("from_z")),
                toX == null ? null : new Location(toX, toY, toZ),
                rows.getObject("distance", Long.class), rows.getInt("owner_id"));
    }
}
