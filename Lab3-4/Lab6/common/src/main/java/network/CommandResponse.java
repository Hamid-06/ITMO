package network;

import models.Route;

import java.io.Serializable;
import java.util.List;

public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final List<Route> routes;

    private CommandResponse(boolean success, String message, List<Route> routes) {
        this.success = success;
        this.message = message;
        this.routes = routes;
    }

    public static CommandResponse ok(String message) {
        return new CommandResponse(true, message, null);
    }

    public static CommandResponse ok(String message, List<Route> routes) {
        return new CommandResponse(true, message, routes);
    }

    public static CommandResponse fail(String message) {
        return new CommandResponse(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Route> getRoutes() { return routes; }
}