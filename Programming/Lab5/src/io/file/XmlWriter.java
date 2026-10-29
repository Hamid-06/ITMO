package io.file;

import exception.FileException;
import models.Route;
import util.XmlUtil;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.TreeSet;

/**
 * Сохраняет коллекцию маршрутов в XML-файл.
 *
 * <p>Использует {@link BufferedOutputStream} для записи (требование задания).</p>
 *
 * @author Hamid
 * @version 1.0
 */
public class XmlWriter {

    private final FileManager fileManager = new FileManager();

    /**
     * Сохраняет коллекцию в файл.
     *
     * @param filename   путь к файлу назначения
     * @param collection коллекция маршрутов
     * @throws FileException если нет прав на запись
     * @throws IOException   если произошла ошибка записи
     */
    public void save(String filename, TreeSet<Route> collection)
            throws FileException, IOException {
        fileManager.checkWritable(filename);

        String xml = XmlUtil.serialize(collection);

        // записываем через BufferedOutputStream (требование задания)
        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filename))) {
            bos.write(xml.getBytes(StandardCharsets.UTF_8));
        }
    }
}