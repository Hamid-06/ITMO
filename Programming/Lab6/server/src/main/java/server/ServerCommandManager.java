package server;

import io.XmlWriter;
import managers.CollectionManager;

import java.util.Scanner;
import java.util.logging.Logger;

public class ServerCommandManager implements Runnable {

    private static final Logger log = Logger.getLogger(ServerCommandManager.class.getName());

    private final CollectionManager manager;
    private final String  filename;
    private final ServerApp serverApp;
    private final XmlWriter  writer = new XmlWriter();

    public ServerCommandManager(CollectionManager manager, String filename, ServerApp serverApp) {
        this.manager   = manager;
        this.filename  = filename;
        this.serverApp = serverApp;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        System.out.println("[SERVER] Server console ready. Commands: save | exit");
        while (true) {
            String line = sc.nextLine().trim();
            switch (line.toLowerCase()) {
                case "save" -> {
                    try {
                        writer.save(filename, manager.getAll());
                        System.out.println("[SERVER] Collection saved to " + filename);
                        log.info("Collection saved manually to " + filename);
                    } catch (Exception e) {
                        System.out.println("[SERVER] Save failed: " + e.getMessage());
                        log.warning("Save failed: " + e.getMessage());
                    }
                }
                case "exit" -> {
                    System.out.println("[SERVER] Shutting down...");
                    serverApp.stop();
                    return;
                }
                default -> System.out.println("[SERVER] Unknown command. Use: save | exit");
            }
        }
    }
}