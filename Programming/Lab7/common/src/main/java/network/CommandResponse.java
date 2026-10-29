package network;

import models.Route;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CommandResponse implements Serializable {
    @Serial private static final long serialVersionUID = 2L;
    public enum Status { OK, ERROR, AUTH_ERROR }

    private final Status status;
    private final String message;
    private final ArrayList<Route> routes;

    private CommandResponse(Status status, String message, List<Route> routes) {
        this.status = Objects.requireNonNull(status);
        this.message = Objects.requireNonNull(message);
        this.routes = new ArrayList<>(routes);
    }

    public static CommandResponse ok(String message) { return ok(message, List.of()); }
    public static CommandResponse ok(String message, List<Route> routes) {
        return new CommandResponse(Status.OK, message, routes);
    }
    public static CommandResponse fail(String message) {
        return new CommandResponse(Status.ERROR, message, List.of());
    }
    public static CommandResponse authError(String message) {
        return new CommandResponse(Status.AUTH_ERROR, message, List.of());
    }

    public boolean isSuccess() { return status == Status.OK; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public List<Route> getRoutes() { return List.copyOf(routes); }
}
