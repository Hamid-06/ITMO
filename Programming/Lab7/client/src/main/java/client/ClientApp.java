package client;

import network.*;
import models.RouteData;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintWriter;

public final class ClientApp {
    @FunctionalInterface
    public interface Sender { CommandResponse send(CommandRequest request) throws IOException; }

    private final Sender sender;
    private final InputStack input;
    private final PrintWriter output;
    private final RouteReader routes;
    private Credentials credentials;

    public ClientApp(Sender sender, InputStack input, PrintWriter output) {
        this.sender = sender;
        this.input = input;
        this.output = output;
        this.routes = new RouteReader(input, output);
    }

    public void run() {
        output.println("Lab 7. Введите register или login. Для завершения: exit.");
        while (true) {
            try {
                String line = input.commandLine();
                if (line == null) break;
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] pieces = line.split("\\s+", 2);
                String name = pieces[0];
                String argument = pieces.length == 2 ? pieces[1].strip() : "";
                if (name.equalsIgnoreCase("logout")) {
                    noArgument(argument);
                    credentials = null;
                    output.println("Вы вышли из учётной записи.");
                    continue;
                }
                CommandType type;
                try {
                    type = CommandType.parse(name);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Неизвестная команда. Введите help после входа.");
                }
                if (type == CommandType.EXIT) {
                    noArgument(argument);
                    break;
                }
                if (type == CommandType.REGISTER || type == CommandType.LOGIN) {
                    noArgument(argument);
                    authenticate(type);
                    continue;
                }
                if (credentials == null) throw new IllegalArgumentException("Сначала выполните register или login.");
                if (type == CommandType.EXECUTE_SCRIPT) {
                    if (argument.isBlank()) throw new IllegalArgumentException("Укажите путь к скрипту.");
                    input.pushScript(unquote(argument));
                    continue;
                }
                CommandResponse response = sender.send(request(type, argument));
                display(response);
                if (response.getStatus() == CommandResponse.Status.AUTH_ERROR) credentials = null;
                if (!response.isSuccess() && input.inScript()) {
                    output.println("Текущий скрипт остановлен из-за ошибки команды.");
                    input.abortScript();
                }
            } catch (EOFException e) {
                if (!input.inScript()) break;
                fail(e.getMessage());
            } catch (IOException e) {
                fail("Ошибка ввода или сети: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                fail(e.getMessage());
            }
        }
    }

    private void authenticate(CommandType type) throws IOException {
        credentials = null;
        String login = input.valueLine("Логин: ", false);
        String password = input.valueLine("Пароль (можно пустой): ", true);
        Credentials candidate = new Credentials(login, password);
        CommandResponse response = sender.send(new CommandRequest(type, candidate));
        display(response);
        if (response.isSuccess()) credentials = candidate;
        else if (input.inScript()) input.abortScript();
    }

    private CommandRequest request(CommandType type, String argument) throws IOException {
        RouteData data = null;
        String text = null;
        Long number = null;
        switch (type.arguments()) {
            case NONE -> noArgument(argument);
            case ROUTE -> {
                noArgument(argument);
                data = routes.read();
            }
            case ID_ROUTE -> {
                number = parseId(argument);
                data = routes.read();
            }
            case ID -> number = parseId(argument);
            case TEXT -> {
                if (argument.isBlank()) throw new IllegalArgumentException("Укажите подстроку.");
                text = argument;
            }
            case DISTANCE -> {
                if (argument.isBlank()) throw new IllegalArgumentException("Укажите дистанцию или null.");
                if (!argument.equalsIgnoreCase("null")) {
                    number = parseNumber(argument);
                    if (number <= 1) throw new IllegalArgumentException("Дистанция должна быть > 1.");
                }
            }
            default -> throw new IllegalArgumentException("Команда не отправляется серверу.");
        }
        return new CommandRequest(type, data, text, number, credentials);
    }

    private static long parseId(String text) {
        long value = parseNumber(text);
        if (value <= 0 || value > Integer.MAX_VALUE) throw new IllegalArgumentException("ID должен быть 1..2147483647.");
        return value;
    }

    private static long parseNumber(String text) {
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Укажите одно целое число.", e);
        }
    }

    private static void noArgument(String text) {
        if (!text.isEmpty()) throw new IllegalArgumentException("Команда не принимает аргумент в этой строке.");
    }

    private static String unquote(String text) {
        return text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")
                ? text.substring(1, text.length() - 1) : text;
    }

    private void display(CommandResponse response) {
        output.println(response.getMessage());
        response.getRoutes().forEach(output::println);
    }

    private void fail(String message) {
        output.println(message);
        if (input.inScript()) {
            output.println("Текущий скрипт остановлен.");
            try {
                input.abortScript();
            } catch (IOException e) {
                output.println("Не удалось закрыть скрипт.");
            }
        }
    }
}
