package client;

import java.io.Console;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public final class ClientMain {
    private ClientMain() { }

    public static void main(String[] args) {
        try {
            if (args.length > 2) throw new IllegalArgumentException("Запуск: client.jar [host] [port]");
            String host = args.length >= 1 ? args[0] : "localhost";
            int port = args.length == 2 ? Integer.parseInt(args[1]) : 8080;
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Порт должен быть 1..65535.");
            Console console = System.console();
            // Console.writer() использует кодировку терминала, в том числе CP866 в Windows.
            // Для IDE и перенаправленных потоков сохраняем UTF-8.
            PrintWriter output = console != null
                    ? new PrintWriter(console.writer(), true)
                    : new PrintWriter(System.out, true, StandardCharsets.UTF_8);
            try (CommandSender sender = new CommandSender(host, port);
                 InputStack input = new InputStack(new InputStreamReader(System.in, StandardCharsets.UTF_8),
                         output, console)) {
                new ClientApp(sender::send, input, output).run();
            }
        } catch (Exception e) {
            System.err.println("Клиент не запущен: " + e.getMessage());
            System.exit(1);
        }
    }
}
