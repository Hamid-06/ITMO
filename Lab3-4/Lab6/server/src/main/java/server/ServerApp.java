package server;

import io.XmlWriter;
import managers.CollectionManager;
import network.CommandRequest;
import network.CommandResponse;
import java.io.*;
import java.net.DatagramSocket;
import java.util.logging.Logger;

public class ServerApp {

    private static final Logger log  = Logger.getLogger(ServerApp.class.getName());
    public  static final int    PORT = 8080;

    private final CollectionManager manager;
    private final String filename;
    private volatile boolean  running = false;

    public ServerApp(CollectionManager manager, String filename) {
        this.manager  = manager;
        this.filename = filename;
    }

    public void start() {
        running = true;
        log.info("=== Server starting on UDP port " + PORT + " (Single-threaded) ===");
        System.out.println("[SERVER] Listening on UDP port " + PORT + "...");

        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            // КРИТИЧЕСКИ ВАЖНО: устанавливаем таймаут ожидания (100 мс)
            socket.setSoTimeout(100);

            RequestReceiver receiver = new RequestReceiver(socket);
            ResponseSender  sender   = new ResponseSender(socket);
            RequestHandler  handler  = new RequestHandler(manager);

            // Для неблокирующей проверки консоли используем BufferedReader
            java.io.BufferedReader consoleReader =
                    new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

            while (running) {
                // 1. Проверяем консоль БЕЗ блокировки потока
                try {
                    if (consoleReader.ready()) { // Есть ли текст в консоли?
                        String line = consoleReader.readLine();
                        if (line != null) {
                            handleServerCommand(line); // Обрабатываем (save, exit и т.д.)
                        }
                    }
                } catch (Exception e) {
                    log.warning("Console error: " + e.getMessage());
                }

                // 2. Проверяем сеть
                try {
                    // Если за 100мс никто не написал, выбросится SocketTimeoutException
                    RequestReceiver.ReceivedRequest received = receiver.receive();
                    CommandRequest request = received.request;

                    CommandResponse response = handler.handle(request);
                    sender.send(response, received.clientAddr, received.clientPort);

                } catch (java.net.SocketTimeoutException e) {
                    // Это не ошибка! Просто за 100 мс никто не прислал пакет.
                    continue;
                } catch (Exception e) {
                    if (running) {
                        log.warning("Error processing request: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.severe("Server failed to start: " + e.getMessage());
        }

        saveOnExit();
    }

    // Вспомогательный метод для обработки команд самого сервера
    private void handleServerCommand(String line) {
        line = line.trim().toLowerCase();
        if ("exit".equals(line)) {
            stop();
        } else if ("save".equals(line)) {
            try {
                new io.XmlWriter().save(filename, manager.getAll());
                System.out.println("[SERVER] Collection saved via console.");
            } catch (Exception e) {
                System.out.println("[SERVER] Failed to save via console: " + e.getMessage());
                log.warning("Failed to save via console: " + e.getMessage());
            }
        } else {
            System.out.println("[SERVER] Unknown command: " + line);
        }
    }

    public void stop() {
        running = false;
        log.info("Server stop requested.");
    }

    private void saveOnExit() {
        try {
            new XmlWriter().save(filename, manager.getAll());
            System.out.println("[SERVER] Collection saved to " + filename + " on exit.");
            log.info("Collection saved to " + filename + " on exit.");
        } catch (Exception e) {
            System.out.println("[SERVER] Failed to save on exit: " + e.getMessage());
            log.warning("Failed to save on exit: " + e.getMessage());
        }
    }
}