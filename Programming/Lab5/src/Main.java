

import commands.CommandDispatcher;
import commands.factory.CommandFactory;
import commands.factory.CommandRegistry;
import exception.FileException;
import io.console.Console;
import io.console.InputHandler;
import io.file.XmlReader;
import managers.CollectionManager;
import script.ScriptExecutor;

import java.io.IOException;

/**
 * Точка входа приложения — менеджера коллекции маршрутов.
 *
 * <p>Порядок инициализации:</p>
 * <ol>
 *   <li>Проверка аргументов командной строки</li>
 *   <li>Создание инфраструктурных объектов (CollectionManager, Console, InputHandler)</li>
 *   <li>Загрузка коллекции из XML-файла</li>
 *   <li>Регистрация команд</li>
 *   <li>Запуск интерактивного цикла</li>
 * </ol>
 *
 * <p>Запуск: {@code java -jar app.jar lab_5.xml}</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class Main {

    /**
     * Точка входа.
     *
     * @param args аргументы командной строки; args[0] — путь к XML-файлу коллекции
     */
    public static void main(String[] args) {


        if (args.length == 0) {
            System.out.println("Использование: java -jar app.jar <lab_5.xml>");
            System.exit(1);
        }
        String filename = args[0];


        CollectionManager manager  = new CollectionManager();
        Console console  = new Console();
        InputHandler input = new InputHandler(console);


        try {
            new XmlReader().load(filename, manager);
        } catch (FileException e) {
            System.out.println("[ПРЕДУПРЕЖДЕНИЕ] " + e.getMessage()
                    + " — коллекция начнёт пустой.");
        } catch (IOException e) {
            System.out.println("[ПРЕДУПРЕЖДЕНИЕ] Ошибка чтения файла: " + e.getMessage()
                    + " — коллекция начнёт пустой.");
        }


        CommandRegistry registry = new CommandRegistry();
        ScriptExecutor executor = new ScriptExecutor(registry);
        CommandFactory factory = new CommandFactory(manager, input, filename, registry, executor);
        factory.registerAll();

             new CommandDispatcher(registry, console, input).run();
    }
}