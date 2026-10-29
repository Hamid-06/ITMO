package io.console;

import java.util.Scanner;

/**
 * Тонкая абстракция над стандартными потоками ввода/вывода.
 *
 * <p>Позволяет легко подменить источник ввода при исполнении скриптов —
 * достаточно передать {@link Scanner} с файлом вместо {@code System.in}.</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class Console {

    /** Источник строк ввода. */
    private Scanner scanner;

    /**
     * Создаёт консоль, читающую из {@code System.in}.
     */
    public Console() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Создаёт консоль с заданным источником ввода.
     *
     * <p>Используется в {@code ScriptExecutor} для чтения из файла.</p>
     *
     * @param scanner источник строк
     */
    public Console(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Заменяет источник ввода.
     *
     * @param scanner новый источник строк
     */
    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Читает следующую строку из источника ввода.
     *
     * @return строка ввода или {@code null} если поток закончился
     */
    public String readLine() {
        if (scanner.hasNextLine()) return scanner.nextLine();
        return null;
    }

    /**
     * Проверяет, есть ли ещё строки для чтения.
     *
     * @return {@code true} если ввод не закончился
     */
    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    /**
     * Выводит строку без переноса (для приглашений ввода).
     *
     * @param message текст приглашения
     */
    public void print(String message) {
        System.out.print(message);
    }

    /**
     * Выводит строку с переносом строки.
     *
     * @param message текст сообщения
     */
    public void println(String message) {
        System.out.println(message);
    }

    /**
     * Выводит сообщение об ошибке с префиксом {@code [ОШИБКА]}.
     *
     * @param message описание ошибки
     */
    public void error(String message) {
        System.out.println("[ОШИБКА] " + message);
    }
}