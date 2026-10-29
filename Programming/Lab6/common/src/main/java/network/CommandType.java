package network;

import java.io.Serializable;

public enum CommandType implements Serializable {
    ADD,
    UPDATE,
    REMOVE_BY_ID,
    CLEAR,
    SHOW,
    INFO,
    HELP,
    ADD_IF_MIN,
    REMOVE_GREATER,
    REMOVE_LOWER,
    REMOVE_ANY_BY_DISTANCE,
    GROUP_COUNTING_BY_COORDINATES,
    FILTER_CONTAINS_NAME,
    EXECUTE_SCRIPT,
    EXIT
}