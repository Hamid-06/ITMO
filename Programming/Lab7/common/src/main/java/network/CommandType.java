package network;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum CommandType {
    ADD(Arguments.ROUTE, "add {element} — добавить маршрут"),
    UPDATE(Arguments.ID_ROUTE, "update id {element} — обновить свой маршрут"),
    REMOVE_BY_ID(Arguments.ID, "remove_by_id id — удалить свой маршрут"),
    CLEAR(Arguments.NONE, "clear — удалить все свои маршруты"),
    SHOW(Arguments.NONE, "show — показать всю коллекцию"),
    INFO(Arguments.NONE, "info — информация о коллекции"),
    HELP(Arguments.NONE, "help — справка"),
    ADD_IF_MIN(Arguments.ROUTE, "add_if_min {element} — добавить, если дистанция меньше всех"),
    REMOVE_GREATER(Arguments.ROUTE, "remove_greater {element} — удалить свои маршруты с большей дистанцией"),
    REMOVE_LOWER(Arguments.ROUTE, "remove_lower {element} — удалить свои маршруты с меньшей дистанцией"),
    REMOVE_ANY_BY_DISTANCE(Arguments.DISTANCE, "remove_any_by_distance distance — удалить один свой маршрут"),
    GROUP_COUNTING_BY_COORDINATES(Arguments.NONE, "group_counting_by_coordinates — количество по координатам"),
    FILTER_CONTAINS_NAME(Arguments.TEXT, "filter_contains_name text — маршруты с подстрокой в имени"),
    EXECUTE_SCRIPT(Arguments.LOCAL, "execute_script file — выполнить команды из UTF-8 файла"),
    EXIT(Arguments.LOCAL, "exit — завершить клиент"),
    REGISTER(Arguments.AUTH, "register — зарегистрироваться и войти"),
    LOGIN(Arguments.AUTH, "login — войти");

    public enum Arguments { NONE, ROUTE, ID_ROUTE, ID, DISTANCE, TEXT, LOCAL, AUTH }

    private final Arguments arguments;
    private final String description;

    CommandType(Arguments arguments, String description) {
        this.arguments = arguments;
        this.description = description;
    }

    public Arguments arguments() { return arguments; }

    public static CommandType parse(String text) {
        return valueOf(text.toUpperCase(Locale.ROOT));
    }

    public static String help() {
        return Arrays.stream(values()).map(c -> c.description).collect(Collectors.joining("\n"))
                + "\nlogout — забыть учётные данные на клиенте"
                + "\nДистанция null меньше любой заданной дистанции. Для null вводите пустую строку при вводе элемента.";
    }
}
