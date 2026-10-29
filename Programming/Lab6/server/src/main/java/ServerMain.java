import io.XmlReader;
import managers.CollectionManager;
import server.ServerApp;

import java.io.IOException;
import java.util.logging.*;

public class ServerMain {

    public static void main(String[] args) {
        setupLogging();

        if (args.length == 0) {
            System.out.println("Usage: java ServerMain <filename.xml>");
            System.exit(1);
        }
        String filename = args[0];

        CollectionManager manager = new CollectionManager();
        try {
            new XmlReader().load(filename, manager);
        } catch (IOException e) {
            System.out.println("[WARN] Could not load collection: " + e.getMessage()
                    + " — starting with empty collection.");
        }

        new ServerApp(manager, filename).start();
    }

    private static void setupLogging() {
        Logger root = Logger.getLogger("");

        // Remove default handlers
        for (Handler h : root.getHandlers()) root.removeHandler(h);

        root.setLevel(Level.ALL);

        // Console handler — INFO and above
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter());
        root.addHandler(consoleHandler);

        // File handler — everything
        try {
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            root.addHandler(fileHandler);
        } catch (IOException e) {
            System.out.println("[WARN] Could not create log file: " + e.getMessage());
        }
    }
}