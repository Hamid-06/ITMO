import client.ClientApp;
import client.CommandSender;
import io.Console;

public class ClientMain {

    private static final String SERVER_HOST = "localhost";
    private static final int    SERVER_PORT = 8080;

    public static void main(String[] args) {
        Console console = new Console();
        CommandSender sender  = new CommandSender(SERVER_HOST, SERVER_PORT);
        new ClientApp(console, sender).run();
    }
}
