package network;

import models.Route;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final CommandType type;
    private final Route routeArg;
    private final String stringArg;
    private final Long longArg;

    public CommandRequest(CommandType type, Route routeArg, String stringArg, Long longArg) {
        this.type = type;
        this.routeArg  = routeArg;
        this.stringArg = stringArg;
        this.longArg = longArg;
    }

    public CommandRequest(CommandType type) {
        this(type, null, null, null);
    }

    public CommandRequest(CommandType type, Route route) {
        this(type, route, null, null);
    }

    public CommandRequest(CommandType type, String stringArg) {
        this(type, null, stringArg, null);
    }

    public CommandRequest(CommandType type, Long longArg) {
        this(type, null, null, longArg);
    }

    public CommandRequest(CommandType type, String stringArg, Route route) {
        this(type, route, stringArg, null);
    }

    public CommandType getType()      { return type; }
    public Route getRouteArg()  { return routeArg; }
    public String getStringArg() { return stringArg; }
    public Long getLongArg()   { return longArg; }

    @Override
    public String toString() {
        return "CommandRequest{type=" + type + ", stringArg=" + stringArg + "}";
    }
}