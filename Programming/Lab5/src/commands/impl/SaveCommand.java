package commands.impl;

import commands.CommandContext;
import commands.base.AbstractCommand;
import commands.base.CommandResult;
import exception.FileException;
import io.file.XmlWriter;
import managers.CollectionManager;

import java.io.IOException;

/**
 * Команда {@code save} — сохраняет коллекцию в XML-файл.
 *
 * @author Hamid
 * @version 1.0
 */
public class SaveCommand extends AbstractCommand {

    /** Путь к файлу для сохранения. */
    private final String filename;

    private final XmlWriter writer = new XmlWriter();

    /**
     * @param manager  менеджер коллекции
     * @param filename путь к файлу
     */
    public SaveCommand(CollectionManager manager, String filename) {
        super(manager);
        this.filename = filename;
    }

    @Override
    public CommandResult execute(CommandContext ctx) {
        try {
            writer.save(filename, manager.getAll());
            return success("Коллекция сохранена в " + filename);
        } catch (FileException | IOException e) {
            return error("Ошибка сохранения: " + e.getMessage());
        }
    }

    @Override public String getName() { return "save"; }
    @Override public String getDescription() { return "сохранить коллекцию в файл"; }
}