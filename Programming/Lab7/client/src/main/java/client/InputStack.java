package client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/** Стек источников сохраняет позицию родительского скрипта при вложенном вызове. */
public final class InputStack implements AutoCloseable {
    private record Script(Path path, BufferedReader reader) { }
    private final BufferedReader terminal;
    private final Console console;
    private final PrintWriter output;
    private final Deque<Script> scripts = new ArrayDeque<>();

    public InputStack(Reader terminal, PrintWriter output) { this(terminal, output, null); }

    public InputStack(Reader terminal, PrintWriter output, Console console) {
        this.terminal = new BufferedReader(terminal);
        this.output = output;
        this.console = console;
    }

    public boolean inScript() { return !scripts.isEmpty(); }

    public String commandLine() throws IOException {
        while (true) {
            if (!scripts.isEmpty()) {
                String line = scripts.peek().reader().readLine();
                if (line == null) {
                    abortScript();
                    continue;
                }
                if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
                return line;
            }
            return terminalLine("> ", false);
        }
    }

    public String valueLine(String prompt, boolean secret) throws IOException {
        String line = scripts.isEmpty() ? terminalLine(prompt, secret) : scripts.peek().reader().readLine();
        if (line == null) throw new EOFException("Ввод закончился до завершения команды.");
        return line;
    }

    public void pushScript(String filename) throws IOException {
        if (scripts.size() >= 16) throw new IOException("Слишком много вложенных скриптов (максимум 16).");
        Path path = Path.of(filename);
        if (!path.isAbsolute() && !scripts.isEmpty()) path = scripts.peek().path().getParent().resolve(path);
        path = path.toRealPath();
        for (Script script : scripts) {
            if (script.path().equals(path)) throw new IOException("Рекурсивный вызов скрипта: " + path);
        }
        scripts.push(new Script(path, Files.newBufferedReader(path, StandardCharsets.UTF_8)));
        output.println("Выполняется скрипт: " + path);
    }

    public void abortScript() throws IOException {
        if (!scripts.isEmpty()) scripts.pop().reader().close();
    }

    private String terminalLine(String prompt, boolean secret) throws IOException {
        if (console != null) {
            if (!secret) return console.readLine("%s", prompt);
            char[] password = console.readPassword("%s", prompt);
            if (password == null) return null;
            try {
                return new String(password);
            } finally {
                Arrays.fill(password, '\0');
            }
        }
        output.print(prompt);
        output.flush();
        return terminal.readLine();
    }

    @Override
    public void close() throws IOException {
        while (!scripts.isEmpty()) abortScript();
        // System.in принадлежит процессу, а не отдельному скрипту.
    }
}
