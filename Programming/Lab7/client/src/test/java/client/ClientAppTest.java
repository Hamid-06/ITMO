package client;

import network.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ClientAppTest {
    @TempDir Path directory;

    private List<CommandRequest> run(String console, StringWriter output) throws Exception {
        List<CommandRequest> requests = new ArrayList<>();
        PrintWriter printer = new PrintWriter(output, true);
        try (InputStack input = new InputStack(new StringReader(console), printer)) {
            new ClientApp(request -> {
                requests.add(request);
                return CommandResponse.ok("OK");
            }, input, printer).run();
        }
        return requests;
    }

    @Test
    void nestedScriptsResolveRelativePathsAndResumeParent() throws Exception {
        Path child = directory.resolve("child.txt");
        Files.writeString(child, "info\n");
        Path parent = directory.resolve("parent.txt");
        Files.writeString(parent, "show\nexecute_script child.txt\nfilter_contains_name Санкт Петербург\n");
        var requests = run("login\nalice\nsecret\nexecute_script \"" + parent + "\"\nexit\n", new StringWriter());
        assertEquals(List.of(CommandType.LOGIN, CommandType.SHOW, CommandType.INFO, CommandType.FILTER_CONTAINS_NAME),
                requests.stream().map(CommandRequest::getType).toList());
        assertEquals("Санкт Петербург", requests.get(3).getStringArg());
        assertTrue(requests.stream().allMatch(r -> r.getCredentials().getPassword().equals("secret")));
    }

    @Test
    void incompleteScriptDoesNotConsumeConsoleAsRouteData() throws Exception {
        Path script = directory.resolve("short.txt");
        Files.writeString(script, "add\nOnly a name\n");
        StringWriter output = new StringWriter();
        var requests = run("login\nalice\nsecret\nexecute_script " + script + "\nshow\nexit\n", output);
        assertEquals(List.of(CommandType.LOGIN, CommandType.SHOW), requests.stream().map(CommandRequest::getType).toList());
        assertTrue(output.toString().contains("Текущий скрипт остановлен"));
    }

    @Test
    void recursionIsDetectedAndConsoleStillWorks() throws Exception {
        Path script = directory.resolve("recursive.txt");
        Files.writeString(script, "execute_script recursive.txt\n");
        StringWriter output = new StringWriter();
        var requests = run("login\nalice\nsecret\nexecute_script " + script + "\ninfo\nexit\n", output);
        assertTrue(output.toString().contains("Рекурсивный вызов"));
        assertEquals(2, requests.size());
        assertEquals(CommandType.INFO, requests.get(1).getType());
    }

    @Test
    void localClientDoesNotSendCommandsBeforeAuthentication() throws Exception {
        var requests = run("show\nadd\nlogout\nexit\n", new StringWriter());
        assertTrue(requests.isEmpty());
    }

    @Test
    void routeFieldsAreParsedAndEmptyDistanceRemainsNull() throws Exception {
        var requests = run("login\nalice\nsecret\nadd\nRoute\n0\n1\n2\n3.5\n4\nn\n\nexit\n", new StringWriter());
        assertEquals(2, requests.size());
        assertEquals(CommandType.ADD, requests.get(1).getType());
        assertNull(requests.get(1).getRouteArg().getDistance());
        assertNull(requests.get(1).getRouteArg().getTo());
        assertEquals(3.5, requests.get(1).getRouteArg().getFrom().getY());
    }
}
