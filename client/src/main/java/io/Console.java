package io;

import java.util.Scanner;

public class Console {

    private Scanner scanner;

    public Console() {
        this.scanner = new Scanner(System.in);
    }

    public Console(Scanner scanner) {
        this.scanner = scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine() {
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    public boolean hasNext() {
        return scanner.hasNextLine();
    }

    public void print(String message) {
        System.out.print(message);
    }

    public void println(String message) {
        System.out.println(message);
    }

    public void error(String message) {
        System.out.println("[ERROR] " + message);
    }
}