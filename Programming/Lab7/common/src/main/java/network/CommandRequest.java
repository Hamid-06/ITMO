package network;

import models.RouteData;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class CommandRequest implements Serializable {
    @Serial private static final long serialVersionUID = 2L;
    private final UUID requestId;
    private final CommandType type;
    private final RouteData routeArg;
    private final String stringArg;
    private final Long longArg;
    private final Credentials credentials;

    public CommandRequest(CommandType type, RouteData routeArg, String stringArg,
                          Long longArg, Credentials credentials) {
        this(UUID.randomUUID(), type, routeArg, stringArg, longArg, credentials);
    }

    public CommandRequest(UUID requestId, CommandType type, RouteData routeArg, String stringArg,
                          Long longArg, Credentials credentials) {
        this.requestId = Objects.requireNonNull(requestId, "requestId");
        this.type = Objects.requireNonNull(type, "type");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
        this.routeArg = routeArg;
        this.stringArg = stringArg;
        this.longArg = longArg;
    }

    public CommandRequest(CommandType type, Credentials credentials) {
        this(type, null, null, null, credentials);
    }

    public CommandRequest(CommandType type, RouteData route, Credentials credentials) {
        this(type, route, null, null, credentials);
    }

    public CommandRequest(CommandType type, String text, Credentials credentials) {
        this(type, null, text, null, credentials);
    }

    public CommandRequest(CommandType type, Long number, Credentials credentials) {
        this(type, null, null, number, credentials);
    }

    public CommandRequest(CommandType type, Long id, RouteData route, Credentials credentials) {
        this(type, route, null, id, credentials);
    }

    public UUID getRequestId() { return requestId; }
    public CommandType getType() { return type; }
    public RouteData getRouteArg() { return routeArg; }
    public String getStringArg() { return stringArg; }
    public Long getLongArg() { return longArg; }
    public Credentials getCredentials() { return credentials; }

    @Serial
    private Object readResolve() {
        return new CommandRequest(requestId, type, routeArg, stringArg, longArg, credentials);
    }

    @Override
    public String toString() { return "CommandRequest{id=" + requestId + ", type=" + type + "}"; }
}
